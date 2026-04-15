package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojplatform.dto.ProblemSetItemDetailDTO;
import com.ojplatform.dto.StandingDTO;
import com.ojplatform.entity.Contest;
import com.ojplatform.entity.ContestStanding;
import com.ojplatform.entity.ContestSubmission;
import com.ojplatform.entity.ContestTeam;
import com.ojplatform.entity.User;
import com.ojplatform.mapper.ContestMapper;
import com.ojplatform.mapper.ContestStandingMapper;
import com.ojplatform.mapper.ContestSubmissionMapper;
import com.ojplatform.mapper.ContestTeamMapper;
import com.ojplatform.mapper.UserMapper;
import com.ojplatform.service.ContestStandingSnapshotService;
import com.ojplatform.service.ProblemSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 比赛榜单快照相关业务实现。
 */
@Service
public class ContestStandingSnapshotServiceImpl implements ContestStandingSnapshotService {

    private static final Duration STANDING_CACHE_TTL = Duration.ofHours(12);
    private static final String STANDING_RANK_KEY_PREFIX = "contest:standing:rank:";
    private static final String STANDING_ROW_KEY_PREFIX = "contest:standing:row:";

    @Autowired
    private ContestMapper contestMapper;

    @Autowired
    private ContestStandingMapper standingMapper;

    @Autowired
    private ContestSubmissionMapper contestSubmissionMapper;

    @Autowired
    private ContestTeamMapper contestTeamMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProblemSetService problemSetService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 读取比赛榜单，优先命中 Redis，未命中时回退到快照表。
     */
    @Override
    @Transactional(readOnly = true)
    public StandingDTO getStanding(Long contestId) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            throw new RuntimeException("比赛不存在");
        }

        boolean frozenView = "frozen".equals(contest.getStatus());
        List<ProblemSetItemDetailDTO> problems = loadContestProblems(contest);
        List<StandingDTO.StandingRow> cachedRows = loadRowsFromCache(contestId, frozenView);
        if (!cachedRows.isEmpty()) {
            return buildStandingDto(contest, problems, cachedRows, frozenView);
        }

        List<ContestStanding> standings = loadSnapshotRows(contestId, frozenView);
        if (standings.isEmpty()) {
            rebuildContestSnapshot(contestId, frozenView);
            standings = loadSnapshotRows(contestId, frozenView);
        }
        refreshCache(contest, standings, frozenView);

        return buildStandingDto(contest, problems, toStandingRows(standings, contest), frozenView);
    }

    /**
     * 在单次提交判定完成后，增量刷新对应参赛者的榜单行。
     */
    @Override
    @Transactional
    public void refreshStandingForSubmission(ContestSubmission submission) {
        if (submission == null) {
            return;
        }

        Contest contest = contestMapper.selectById(submission.getContestId());
        if (contest == null || "frozen".equals(contest.getStatus())) {
            return;
        }

        boolean teamContest = "team".equals(contest.getContestType());
        Long participantId = teamContest ? submission.getTeamId() : submission.getUserId();
        if (participantId == null) {
            return;
        }

        upsertParticipantStanding(contest, participantId, teamContest, false);
        rerankContest(contest, false);
        refreshCache(contest, loadSnapshotRows(contest.getId(), false), false);
    }

    /**
     * 重建整场比赛的榜单快照，主要用于封榜、解榜和缓存回填。
     */
    @Override
    @Transactional
    public void rebuildContestSnapshot(Long contestId, boolean frozenView) {
        Contest contest = contestMapper.selectById(contestId);
        if (contest == null) {
            return;
        }

        boolean teamContest = "team".equals(contest.getContestType());
        LocalDateTime freezeTime = resolveFreezeTime(contest, frozenView);
        List<ProblemSetItemDetailDTO> problems = loadContestProblems(contest);
        List<ContestSubmission> submissions = loadFinishedSubmissions(contestId, null, teamContest, freezeTime, false);

        standingMapper.delete(new LambdaQueryWrapper<ContestStanding>()
                .eq(ContestStanding::getContestId, contestId)
                .eq(ContestStanding::getIsFrozen, frozenView));

        Map<Long, List<ContestSubmission>> grouped = submissions.stream()
                .filter(s -> teamContest ? s.getTeamId() != null : s.getUserId() != null)
                .collect(Collectors.groupingBy(teamContest ? ContestSubmission::getTeamId : ContestSubmission::getUserId,
                        LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Long, List<ContestSubmission>> entry : grouped.entrySet()) {
            ContestStanding standing = buildStandingEntity(
                    contest,
                    problems,
                    entry.getKey(),
                    teamContest,
                    entry.getValue(),
                    frozenView
            );
            standingMapper.insert(standing);
        }

        rerankContest(contest, frozenView);
        refreshCache(contest, loadSnapshotRows(contest.getId(), frozenView), frozenView);
    }

    private StandingDTO buildStandingDto(Contest contest,
                                         List<ProblemSetItemDetailDTO> problems,
                                         List<StandingDTO.StandingRow> rows,
                                         boolean frozenView) {
        StandingDTO dto = new StandingDTO();
        dto.setContestId(contest.getId());
        dto.setScoringRule(contest.getScoringRule());
        dto.setFrozen(frozenView);
        dto.setProblems(toStandingProblems(problems));
        dto.setRows(rows);
        return dto;
    }

    private List<ContestStanding> loadSnapshotRows(Long contestId, boolean frozenView) {
        return standingMapper.selectList(
                new LambdaQueryWrapper<ContestStanding>()
                        .eq(ContestStanding::getContestId, contestId)
                        .eq(ContestStanding::getIsFrozen, frozenView)
                        .orderByAsc(ContestStanding::getRank)
        );
    }

    private void upsertParticipantStanding(Contest contest, Long participantId, boolean teamContest, boolean frozenView) {
        List<ProblemSetItemDetailDTO> problems = loadContestProblems(contest);
        List<ContestSubmission> submissions = loadFinishedSubmissions(contest.getId(), participantId, teamContest, null, false);
        ContestStanding existing = findStanding(contest.getId(), participantId, teamContest, frozenView);

        if (submissions.isEmpty()) {
            if (existing != null) {
                standingMapper.deleteById(existing.getId());
            }
            return;
        }

        ContestStanding standing = buildStandingEntity(contest, problems, participantId, teamContest, submissions, frozenView);
        if (existing != null) {
            standing.setId(existing.getId());
            standingMapper.updateById(standing);
        } else {
            standingMapper.insert(standing);
        }
    }

    private ContestStanding findStanding(Long contestId, Long participantId, boolean teamContest, boolean frozenView) {
        LambdaQueryWrapper<ContestStanding> wrapper = new LambdaQueryWrapper<ContestStanding>()
                .eq(ContestStanding::getContestId, contestId)
                .eq(ContestStanding::getIsFrozen, frozenView)
                .last("limit 1");

        if (teamContest) {
            wrapper.eq(ContestStanding::getTeamId, participantId)
                    .isNull(ContestStanding::getUserId);
        } else {
            wrapper.eq(ContestStanding::getUserId, participantId)
                    .isNull(ContestStanding::getTeamId);
        }
        return standingMapper.selectOne(wrapper);
    }

    private ContestStanding buildStandingEntity(Contest contest,
                                                List<ProblemSetItemDetailDTO> problems,
                                                Long participantId,
                                                boolean teamContest,
                                                List<ContestSubmission> submissions,
                                                boolean frozenView) {
        StandingDTO.StandingRow row = buildStandingRow(contest, problems, participantId, teamContest, submissions, frozenView);
        ContestStanding standing = new ContestStanding();
        standing.setContestId(contest.getId());
        standing.setUserId(teamContest ? null : participantId);
        standing.setTeamId(teamContest ? participantId : null);
        standing.setRank(0);
        standing.setSolvedCount(row.getSolvedCount());
        standing.setTotalScore(row.getTotalScore());
        standing.setTotalPenalty(row.getTotalPenalty());
        standing.setIsFrozen(frozenView);
        standing.setSnapshotTime(LocalDateTime.now());
        try {
            standing.setProblemDetails(objectMapper.writeValueAsString(row.getProblemResults()));
        } catch (Exception e) {
            throw new RuntimeException("榜单快照序列化失败", e);
        }
        return standing;
    }

    private StandingDTO.StandingRow buildStandingRow(Contest contest,
                                                     List<ProblemSetItemDetailDTO> problems,
                                                     Long participantId,
                                                     boolean teamContest,
                                                     List<ContestSubmission> submissions,
                                                     boolean frozenView) {
        StandingDTO.StandingRow row = new StandingDTO.StandingRow();
        if (teamContest) {
            row.setTeamId(participantId);
        } else {
            row.setUserId(participantId);
        }

        LocalDateTime freezeTime = resolveFreezeTime(contest, frozenView);
        int solvedCount = 0;
        int totalScore = 0;
        long totalPenalty = 0L;
        List<StandingDTO.ProblemResult> results = new ArrayList<>();

        for (ProblemSetItemDetailDTO item : problems) {
            List<ContestSubmission> problemSubs = submissions.stream()
                    .filter(s -> item.getProblemId().equals(s.getProblemId()))
                    .collect(Collectors.toList());

            List<ContestSubmission> visibleSubs = problemSubs;
            boolean hiddenAfterFreeze = false;
            if (freezeTime != null) {
                hiddenAfterFreeze = problemSubs.stream().anyMatch(s -> s.getSubmittedAt().isAfter(freezeTime));
                visibleSubs = problemSubs.stream()
                        .filter(s -> !s.getSubmittedAt().isAfter(freezeTime))
                        .collect(Collectors.toList());
            }

            StandingDTO.ProblemResult result = new StandingDTO.ProblemResult();
            result.setProblemId(item.getProblemId());
            result.setAccepted(false);
            result.setAttempts(0);
            result.setScore(0);
            result.setFrozen(hiddenAfterFreeze);

            switch (contest.getScoringRule()) {
                case "acm" -> applyAcmScore(result, visibleSubs, contest, item);
                case "oi" -> applyOiScore(result, visibleSubs);
                case "cf" -> applyCfScore(result, visibleSubs, contest, item);
                default -> applyAcmScore(result, visibleSubs, contest, item);
            }

            if (Boolean.TRUE.equals(result.getAccepted())) {
                solvedCount++;
            }
            totalScore += result.getScore() != null ? result.getScore() : 0;
            if (Boolean.TRUE.equals(result.getAccepted()) && result.getFirstAcTimeSeconds() != null) {
                totalPenalty += result.getFirstAcTimeSeconds()
                        + (long) Math.max(0, result.getAttempts() - 1) * contest.getPenaltyTime() * 60;
            }
            results.add(result);
        }

        row.setSolvedCount(solvedCount);
        row.setTotalScore(totalScore);
        row.setTotalPenalty(totalPenalty);
        row.setProblemResults(results);
        return row;
    }

    private void rerankContest(Contest contest, boolean frozenView) {
        List<ContestStanding> standings = loadSnapshotRows(contest.getId(), frozenView);
        Comparator<ContestStanding> comparator;
        switch (contest.getScoringRule()) {
            case "oi", "cf" -> comparator = Comparator
                    .comparingInt(ContestStanding::getTotalScore).reversed()
                    .thenComparingLong(ContestStanding::getTotalPenalty);
            case "acm" -> comparator = Comparator
                    .comparingInt(ContestStanding::getSolvedCount).reversed()
                    .thenComparingLong(ContestStanding::getTotalPenalty);
            default -> comparator = Comparator
                    .comparingInt(ContestStanding::getSolvedCount).reversed()
                    .thenComparingLong(ContestStanding::getTotalPenalty);
        }

        standings.sort(comparator);
        for (int i = 0; i < standings.size(); i++) {
            ContestStanding standing = standings.get(i);
            standing.setRank(i + 1);
            standing.setSnapshotTime(LocalDateTime.now());
            standingMapper.updateById(standing);
        }
    }

    private List<StandingDTO.StandingRow> loadRowsFromCache(Long contestId, boolean frozenView) {
        String rankKey = buildRankKey(contestId, frozenView);
        String rowKey = buildRowKey(contestId, frozenView);
        Long size = stringRedisTemplate.opsForZSet().zCard(rankKey);
        if (size == null || size == 0) {
            return List.of();
        }

        Set<String> members = stringRedisTemplate.opsForZSet().reverseRange(rankKey, 0, -1);
        if (members == null || members.isEmpty()) {
            return List.of();
        }

        List<Object> values = stringRedisTemplate.opsForHash().multiGet(rowKey, new ArrayList<>(members));
        if (values == null || values.isEmpty()) {
            return List.of();
        }

        List<StandingDTO.StandingRow> rows = new ArrayList<>();
        for (Object value : values) {
            if (!(value instanceof String json) || json.isBlank()) {
                return List.of();
            }
            try {
                rows.add(objectMapper.readValue(json, StandingDTO.StandingRow.class));
            } catch (Exception e) {
                return List.of();
            }
        }
        return rows;
    }

    /**
     * 把快照结果写入 Redis 排名集合和详情缓存。
     */
    private void refreshCache(Contest contest, List<ContestStanding> standings, boolean frozenView) {
        String rankKey = buildRankKey(contest.getId(), frozenView);
        String rowKey = buildRowKey(contest.getId(), frozenView);
        stringRedisTemplate.delete(List.of(rankKey, rowKey));

        if (standings.isEmpty()) {
            return;
        }

        boolean teamContest = "team".equals(contest.getContestType());
        Map<String, String> rowPayload = new LinkedHashMap<>();
        List<StandingDTO.StandingRow> rows = toStandingRows(standings, contest);
        for (int i = 0; i < standings.size(); i++) {
            ContestStanding standing = standings.get(i);
            String member = buildMemberKey(standing, teamContest);
            if (member == null) {
                continue;
            }
            try {
                rowPayload.put(member, objectMapper.writeValueAsString(rows.get(i)));
            } catch (Exception e) {
                throw new RuntimeException("榜单缓存序列化失败", e);
            }
            stringRedisTemplate.opsForZSet().add(rankKey, member, -standing.getRank());
        }

        if (!rowPayload.isEmpty()) {
            stringRedisTemplate.opsForHash().putAll(rowKey, rowPayload);
            stringRedisTemplate.expire(rankKey, STANDING_CACHE_TTL);
            stringRedisTemplate.expire(rowKey, STANDING_CACHE_TTL);
        }
    }

    private String buildRankKey(Long contestId, boolean frozenView) {
        return STANDING_RANK_KEY_PREFIX + contestId + ":" + (frozenView ? "frozen" : "live");
    }

    private String buildRowKey(Long contestId, boolean frozenView) {
        return STANDING_ROW_KEY_PREFIX + contestId + ":" + (frozenView ? "frozen" : "live");
    }

    private String buildMemberKey(ContestStanding standing, boolean teamContest) {
        if (teamContest) {
            return standing.getTeamId() == null ? null : "t:" + standing.getTeamId();
        }
        return standing.getUserId() == null ? null : "u:" + standing.getUserId();
    }

    private List<ProblemSetItemDetailDTO> loadContestProblems(Contest contest) {
        if (contest.getProblemSetId() == null) {
            return List.of();
        }
        return problemSetService.getProblemSetItems(contest.getProblemSetId());
    }

    private List<StandingDTO.StandingProblem> toStandingProblems(List<ProblemSetItemDetailDTO> problems) {
        return problems.stream().map(item -> {
            StandingDTO.StandingProblem problem = new StandingDTO.StandingProblem();
            problem.setProblemId(item.getProblemId());
            problem.setSlug(item.getSlug());
            problem.setTitle(item.getTitle());
            problem.setFrontendId(item.getFrontendId());
            problem.setScore(item.getScore());
            return problem;
        }).collect(Collectors.toList());
    }

    private List<StandingDTO.StandingRow> toStandingRows(List<ContestStanding> standings, Contest contest) {
        boolean teamContest = "team".equals(contest.getContestType());
        return standings.stream().map(standing -> {
            StandingDTO.StandingRow row = new StandingDTO.StandingRow();
            row.setRank(standing.getRank());
            row.setSolvedCount(standing.getSolvedCount());
            row.setTotalScore(standing.getTotalScore());
            row.setTotalPenalty(standing.getTotalPenalty());
            if (teamContest) {
                row.setTeamId(standing.getTeamId());
                ContestTeam team = standing.getTeamId() != null ? contestTeamMapper.selectById(standing.getTeamId()) : null;
                row.setTeamName(team != null ? team.getTeamName() : "未知队伍");
            } else {
                row.setUserId(standing.getUserId());
                User user = standing.getUserId() != null ? userMapper.selectById(standing.getUserId()) : null;
                row.setUsername(user != null ? user.getUsername() : "未知用户");
            }
            row.setProblemResults(parseProblemResults(standing.getProblemDetails()));
            return row;
        }).collect(Collectors.toList());
    }

    private List<StandingDTO.ProblemResult> parseProblemResults(String problemDetails) {
        if (problemDetails == null || problemDetails.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(problemDetails, new TypeReference<List<StandingDTO.ProblemResult>>() {});
        } catch (Exception e) {
            throw new RuntimeException("榜单快照反序列化失败", e);
        }
    }

    private List<ContestSubmission> loadFinishedSubmissions(Long contestId,
                                                            Long participantId,
                                                            boolean teamContest,
                                                            LocalDateTime freezeTime,
                                                            boolean visibleOnly) {
        LambdaQueryWrapper<ContestSubmission> wrapper = new LambdaQueryWrapper<ContestSubmission>()
                .eq(ContestSubmission::getContestId, contestId)
                .ne(ContestSubmission::getStatus, "Pending")
                .orderByAsc(ContestSubmission::getSubmittedAt);

        if (participantId != null) {
            if (teamContest) {
                wrapper.eq(ContestSubmission::getTeamId, participantId);
            } else {
                wrapper.eq(ContestSubmission::getUserId, participantId);
            }
        }
        if (freezeTime != null && visibleOnly) {
            wrapper.le(ContestSubmission::getSubmittedAt, freezeTime);
        }
        return contestSubmissionMapper.selectList(wrapper);
    }

    private LocalDateTime resolveFreezeTime(Contest contest, boolean frozenView) {
        if (!frozenView || contest.getFreezeMinutes() == null || contest.getFreezeMinutes() <= 0) {
            return null;
        }
        return contest.getEndTime().minusMinutes(contest.getFreezeMinutes());
    }

    private void applyAcmScore(StandingDTO.ProblemResult result,
                               List<ContestSubmission> submissions,
                               Contest contest,
                               ProblemSetItemDetailDTO item) {
        int attempts = 0;
        for (ContestSubmission submission : submissions) {
            attempts++;
            if ("Accepted".equals(submission.getStatus())) {
                result.setAccepted(true);
                result.setAttempts(attempts);
                result.setFirstAcTimeSeconds(Duration.between(contest.getStartTime(), submission.getSubmittedAt()).getSeconds());
                result.setScore(item.getScore());
                return;
            }
        }
        result.setAttempts(attempts);
    }

    private void applyOiScore(StandingDTO.ProblemResult result, List<ContestSubmission> submissions) {
        if (submissions.isEmpty()) {
            return;
        }
        ContestSubmission last = submissions.get(submissions.size() - 1);
        result.setAttempts(submissions.size());
        result.setScore(last.getScore());
        result.setAccepted("Accepted".equals(last.getStatus()));
    }

    private void applyCfScore(StandingDTO.ProblemResult result,
                              List<ContestSubmission> submissions,
                              Contest contest,
                              ProblemSetItemDetailDTO item) {
        int attempts = 0;
        for (ContestSubmission submission : submissions) {
            attempts++;
            if ("Accepted".equals(submission.getStatus())) {
                result.setAccepted(true);
                result.setAttempts(attempts);
                long acTimeMinutes = Duration.between(contest.getStartTime(), submission.getSubmittedAt()).toMinutes();
                result.setFirstAcTimeSeconds(acTimeMinutes * 60);
                int maxScore = item.getScore();
                int score = (int) Math.max(
                        3.0 * maxScore / 10,
                        maxScore - (double) maxScore / 250 * acTimeMinutes - 50.0 * (attempts - 1)
                );
                result.setScore(score);
                return;
            }
        }
        result.setAttempts(attempts);
    }
}
