package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ojplatform.dto.*;
import com.ojplatform.entity.*;
import com.ojplatform.mapper.*;
import com.ojplatform.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 比赛服务实现类
 * 负责比赛全生命周期管理：创建、报名、组队、提交、榜单计算、封榜
 */
@Service
public class ContestServiceImpl extends ServiceImpl<ContestMapper, Contest> implements ContestService {

    private static final Logger log = LoggerFactory.getLogger(ContestServiceImpl.class);

    @Autowired
    private ContestRegistrationMapper registrationMapper;
    @Autowired
    private ContestTeamMapper teamMapper;
    @Autowired
    private ContestTeamMemberMapper teamMemberMapper;
    @Autowired
    private ContestSubmissionMapper contestSubmissionMapper;
    @Autowired
    private ContestStandingMapper standingMapper;
    @Autowired
    private ProblemSetService problemSetService;
    @Autowired
    private ProblemSetItemMapper problemSetItemMapper;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private OjApiServiceFactory ojApiServiceFactory;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Contest createContest(CreateContestDTO dto) {
        // 1. 创建比赛实体
        Contest contest = new Contest();
        contest.setCreatorId(dto.getUserId());
        contest.setTitle(dto.getTitle());
        contest.setDescription(dto.getDescription());
        contest.setContestType(dto.getContestType());
        contest.setStatus("draft");
        contest.setStartTime(dto.getStartTime());
        contest.setEndTime(dto.getStartTime().plusMinutes(dto.getDurationMinutes()));
        contest.setDurationMinutes(dto.getDurationMinutes());
        contest.setFreezeMinutes(dto.getFreezeMinutes());
        contest.setMaxParticipants(dto.getMaxParticipants());
        contest.setMaxTeamSize(dto.getMaxTeamSize());
        contest.setScoringRule(dto.getScoringRule());
        contest.setPenaltyTime(dto.getPenaltyTime());
        contest.setIsPublic(dto.getIsPublic());
        contest.setOjPlatform(dto.getOjPlatform());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            contest.setPassword(dto.getPassword());
        }

        if (dto.getAllowLanguage() != null) {
            try {
                contest.setAllowLanguage(objectMapper.writeValueAsString(dto.getAllowLanguage()));
            } catch (Exception e) {
                log.warn("序列化允许语言失败", e);
            }
        }

        // 2. 处理题目来源
        if ("existing_set".equals(dto.getProblemSource()) && dto.getProblemSetId() != null) {
            contest.setProblemSetId(dto.getProblemSetId());
        } else if ("manual".equals(dto.getProblemSource()) && dto.getProblems() != null && !dto.getProblems().isEmpty()) {
            // 手动选题 → 自动创建题单
            CreateProblemSetDTO psDto = new CreateProblemSetDTO();
            psDto.setUserId(dto.getUserId());
            psDto.setTitle(dto.getTitle() + " - 比赛题单");
            psDto.setOjPlatform(dto.getOjPlatform());
            List<CreateProblemSetDTO.ProblemItem> items = dto.getProblems().stream().map(cp -> {
                CreateProblemSetDTO.ProblemItem pi = new CreateProblemSetDTO.ProblemItem();
                pi.setSlug(cp.getSlug());
                pi.setScore(cp.getScore());
                return pi;
            }).collect(Collectors.toList());
            psDto.setProblems(items);
            ProblemSet ps = problemSetService.createProblemSet(psDto);
            ps.setVisibility("contest_only");
            ps.setStatus("published");
            problemSetService.updateById(ps);
            contest.setProblemSetId(ps.getId());
        }

        baseMapper.insert(contest);
        log.info("创建比赛：id={}, title={}, type={}", contest.getId(), contest.getTitle(), contest.getContestType());
        return contest;
    }

    @Override
    @Transactional
    public void updateContest(Long contestId, CreateContestDTO dto) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) throw new RuntimeException("比赛不存在");
        if (!contest.getCreatorId().equals(dto.getUserId())) throw new RuntimeException("无权操作");
        if (!"draft".equals(contest.getStatus())) throw new RuntimeException("只有草稿状态的比赛才能编辑");

        // 更新基本信息
        if (dto.getTitle() != null) contest.setTitle(dto.getTitle());
        if (dto.getDescription() != null) contest.setDescription(dto.getDescription());
        if (dto.getContestType() != null) contest.setContestType(dto.getContestType());
        if (dto.getStartTime() != null) {
            contest.setStartTime(dto.getStartTime());
            contest.setEndTime(dto.getStartTime().plusMinutes(dto.getDurationMinutes()));
        }
        if (dto.getDurationMinutes() != null) {
            contest.setDurationMinutes(dto.getDurationMinutes());
            if (contest.getStartTime() != null) {
                contest.setEndTime(contest.getStartTime().plusMinutes(dto.getDurationMinutes()));
            }
        }
        if (dto.getFreezeMinutes() != null) contest.setFreezeMinutes(dto.getFreezeMinutes());
        if (dto.getMaxParticipants() != null) contest.setMaxParticipants(dto.getMaxParticipants());
        if (dto.getMaxTeamSize() != null) contest.setMaxTeamSize(dto.getMaxTeamSize());
        if (dto.getScoringRule() != null) contest.setScoringRule(dto.getScoringRule());
        if (dto.getPenaltyTime() != null) contest.setPenaltyTime(dto.getPenaltyTime());
        if (dto.getIsPublic() != null) contest.setIsPublic(dto.getIsPublic());
        if (dto.getPassword() != null) contest.setPassword(dto.getPassword().isBlank() ? null : dto.getPassword());
        if (dto.getAllowLanguage() != null) {
            try { contest.setAllowLanguage(objectMapper.writeValueAsString(dto.getAllowLanguage())); } catch (Exception ignored) {}
        }

        // 更新题目（如果提供了新题目列表）
        if (dto.getProblems() != null && !dto.getProblems().isEmpty()) {
            // 创建新题单替换旧的
            CreateProblemSetDTO psDto = new CreateProblemSetDTO();
            psDto.setUserId(dto.getUserId());
            psDto.setTitle(contest.getTitle() + " - 比赛题单");
            psDto.setOjPlatform(dto.getOjPlatform() != null ? dto.getOjPlatform() : "leetcode");
            List<CreateProblemSetDTO.ProblemItem> items = dto.getProblems().stream().map(cp -> {
                CreateProblemSetDTO.ProblemItem pi = new CreateProblemSetDTO.ProblemItem();
                pi.setSlug(cp.getSlug());
                pi.setScore(cp.getScore());
                return pi;
            }).collect(Collectors.toList());
            psDto.setProblems(items);
            ProblemSet ps = problemSetService.createProblemSet(psDto);
            ps.setVisibility("contest_only");
            ps.setStatus("published");
            problemSetService.updateById(ps);

            // 删除旧题单（如果有）
            if (contest.getProblemSetId() != null) {
                try { problemSetService.removeById(contest.getProblemSetId()); } catch (Exception ignored) {}
            }
            contest.setProblemSetId(ps.getId());
        } else if ("existing_set".equals(dto.getProblemSource()) && dto.getProblemSetId() != null) {
            contest.setProblemSetId(dto.getProblemSetId());
        }

        baseMapper.updateById(contest);
        log.info("更新比赛：id={}", contestId);
    }

    @Override
    public IPage<ContestDetailDTO> listContests(Long userId, String filter, String keyword, String status, int pageNum, int pageSize) {
        // 先查比赛列表
        LambdaQueryWrapper<Contest> wrapper = new LambdaQueryWrapper<Contest>()
                .orderByDesc(Contest::getStartTime);

        // 关键词搜索
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Contest::getTitle, keyword.trim());
        }

        // 状态过滤
        if (status != null && !status.isBlank()) {
            wrapper.eq(Contest::getStatus, status.trim());
        }

        if ("my_created".equals(filter)) {
            wrapper.eq(Contest::getCreatorId, userId);
        } else if (!"my_joined".equals(filter)) {
            // all 模式：草稿态只显示自己创建的
            wrapper.and(w -> w
                    .ne(Contest::getStatus, "draft")
                    .or(o -> o.eq(Contest::getStatus, "draft").eq(Contest::getCreatorId, userId))
            );
        }

        IPage<Contest> contestPage = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        // 如果是 my_joined，需要过滤
        if ("my_joined".equals(filter)) {
            List<Long> joinedContestIds = registrationMapper.selectList(
                    new LambdaQueryWrapper<ContestRegistration>()
                            .eq(ContestRegistration::getUserId, userId)
                            .eq(ContestRegistration::getStatus, "registered")
            ).stream().map(ContestRegistration::getContestId).collect(Collectors.toList());

            if (joinedContestIds.isEmpty()) {
                Page<ContestDetailDTO> emptyPage = new Page<>(pageNum, pageSize);
                emptyPage.setRecords(List.of());
                emptyPage.setTotal(0);
                return emptyPage;
            }

            wrapper = new LambdaQueryWrapper<Contest>()
                    .in(Contest::getId, joinedContestIds)
                    .orderByDesc(Contest::getStartTime);
            if (keyword != null && !keyword.isBlank()) {
                wrapper.like(Contest::getTitle, keyword.trim());
            }
            if (status != null && !status.isBlank()) {
                wrapper.eq(Contest::getStatus, status.trim());
            }
            contestPage = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        }

        // 转换为 DetailDTO
        Page<ContestDetailDTO> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(contestPage.getTotal());
        resultPage.setRecords(contestPage.getRecords().stream()
                .map(c -> toContestDetailDTO(c, userId))
                .collect(Collectors.toList()));
        return resultPage;
    }

    @Override
    public ContestDetailDTO getContestDetail(Long contestId, Long userId) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) throw new RuntimeException("比赛不存在");
        refreshContestStatus(contestId);
        contest = baseMapper.selectById(contestId);
        return toContestDetailDTO(contest, userId);
    }

    @Override
    public void publishContest(Long contestId, Long userId) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) throw new RuntimeException("比赛不存在");
        if (!contest.getCreatorId().equals(userId)) throw new RuntimeException("无权操作");
        if (!"draft".equals(contest.getStatus())) throw new RuntimeException("只有草稿状态的比赛才能发布");
        if (contest.getProblemSetId() == null) throw new RuntimeException("请先设置比赛题目");

        contest.setStatus("registering");
        baseMapper.updateById(contest);
        log.info("比赛已发布：id={}", contestId);
    }

    @Override
    @Transactional
    public void registerContest(Long contestId, Long userId, String password) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) throw new RuntimeException("比赛不存在");
        refreshContestStatus(contestId);
        contest = baseMapper.selectById(contestId);

        if (!"registering".equals(contest.getStatus()) && !"running".equals(contest.getStatus())) {
            throw new RuntimeException("当前不在报名/进行阶段");
        }

        // 密码校验
        if (contest.getPassword() != null && !contest.getPassword().isBlank()) {
            if (password == null || !contest.getPassword().equals(password)) {
                throw new RuntimeException("比赛密码错误");
            }
        }

        // 检查人数限制
        if (contest.getMaxParticipants() > 0) {
            Long count = registrationMapper.selectCount(
                    new LambdaQueryWrapper<ContestRegistration>()
                            .eq(ContestRegistration::getContestId, contestId)
                            .eq(ContestRegistration::getStatus, "registered")
            );
            if (count >= contest.getMaxParticipants()) {
                throw new RuntimeException("报名人数已满");
            }
        }

        // 检查是否已报名
        Long exists = registrationMapper.selectCount(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getContestId, contestId)
                        .eq(ContestRegistration::getUserId, userId)
                        .eq(ContestRegistration::getStatus, "registered")
        );
        if (exists > 0) throw new RuntimeException("已报名该比赛");

        ContestRegistration reg = new ContestRegistration();
        reg.setContestId(contestId);
        reg.setUserId(userId);
        reg.setStatus("registered");
        registrationMapper.insert(reg);
        log.info("用户报名比赛：userId={}, contestId={}", userId, contestId);
    }

    @Override
    public void cancelRegistration(Long contestId, Long userId) {
        ContestRegistration reg = registrationMapper.selectOne(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getContestId, contestId)
                        .eq(ContestRegistration::getUserId, userId)
                        .eq(ContestRegistration::getStatus, "registered")
        );
        if (reg == null) throw new RuntimeException("未报名该比赛");

        reg.setStatus("cancelled");
        registrationMapper.updateById(reg);
    }

    @Override
    @Transactional
    public ContestTeam createTeam(Long contestId, CreateTeamDTO dto) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) throw new RuntimeException("比赛不存在");
        if (!"team".equals(contest.getContestType())) throw new RuntimeException("非组队赛不能创建队伍");

        // 生成邀请码
        String inviteCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        ContestTeam team = new ContestTeam();
        team.setContestId(contestId);
        team.setTeamName(dto.getTeamName());
        team.setCaptainId(dto.getUserId());
        team.setInviteCode(inviteCode);
        team.setMemberCount(1);
        teamMapper.insert(team);

        // 队长加入队伍
        ContestTeamMember member = new ContestTeamMember();
        member.setTeamId(team.getId());
        member.setUserId(dto.getUserId());
        member.setRole("captain");
        teamMemberMapper.insert(member);

        // 自动报名比赛
        Long regExists = registrationMapper.selectCount(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getContestId, contestId)
                        .eq(ContestRegistration::getUserId, dto.getUserId())
                        .eq(ContestRegistration::getStatus, "registered")
        );
        if (regExists == 0) {
            ContestRegistration reg = new ContestRegistration();
            reg.setContestId(contestId);
            reg.setUserId(dto.getUserId());
            reg.setTeamId(team.getId());
            reg.setStatus("registered");
            registrationMapper.insert(reg);
        } else {
            // 更新已有报名记录的 teamId
            ContestRegistration reg = registrationMapper.selectOne(
                    new LambdaQueryWrapper<ContestRegistration>()
                            .eq(ContestRegistration::getContestId, contestId)
                            .eq(ContestRegistration::getUserId, dto.getUserId())
                            .eq(ContestRegistration::getStatus, "registered")
            );
            reg.setTeamId(team.getId());
            registrationMapper.updateById(reg);
        }

        log.info("创建队伍：contestId={}, teamName={}, inviteCode={}", contestId, dto.getTeamName(), inviteCode);
        return team;
    }

    @Override
    @Transactional
    public void joinTeam(Long contestId, JoinTeamDTO dto) {
        ContestTeam team = teamMapper.selectOne(
                new LambdaQueryWrapper<ContestTeam>()
                        .eq(ContestTeam::getContestId, contestId)
                        .eq(ContestTeam::getInviteCode, dto.getInviteCode())
        );
        if (team == null) throw new RuntimeException("邀请码无效");

        Contest contest = baseMapper.selectById(contestId);
        if (team.getMemberCount() >= contest.getMaxTeamSize()) {
            throw new RuntimeException("队伍已满（最多 " + contest.getMaxTeamSize() + " 人）");
        }

        // 检查是否已在队伍中
        Long exists = teamMemberMapper.selectCount(
                new LambdaQueryWrapper<ContestTeamMember>()
                        .eq(ContestTeamMember::getTeamId, team.getId())
                        .eq(ContestTeamMember::getUserId, dto.getUserId())
        );
        if (exists > 0) throw new RuntimeException("你已在该队伍中");

        ContestTeamMember member = new ContestTeamMember();
        member.setTeamId(team.getId());
        member.setUserId(dto.getUserId());
        member.setRole("member");
        teamMemberMapper.insert(member);

        team.setMemberCount(team.getMemberCount() + 1);
        teamMapper.updateById(team);

        // 自动报名
        Long regExists = registrationMapper.selectCount(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getContestId, contestId)
                        .eq(ContestRegistration::getUserId, dto.getUserId())
                        .eq(ContestRegistration::getStatus, "registered")
        );
        if (regExists == 0) {
            ContestRegistration reg = new ContestRegistration();
            reg.setContestId(contestId);
            reg.setUserId(dto.getUserId());
            reg.setTeamId(team.getId());
            reg.setStatus("registered");
            registrationMapper.insert(reg);
        }
    }

    @Override
    @Transactional
    public void leaveTeam(Long contestId, Long teamId, Long userId) {
        ContestTeam team = teamMapper.selectById(teamId);
        if (team == null) throw new RuntimeException("队伍不存在");
        if (team.getCaptainId().equals(userId)) throw new RuntimeException("队长不能退出队伍，请先转让队长或解散队伍");

        teamMemberMapper.delete(
                new LambdaQueryWrapper<ContestTeamMember>()
                        .eq(ContestTeamMember::getTeamId, teamId)
                        .eq(ContestTeamMember::getUserId, userId)
        );
        team.setMemberCount(team.getMemberCount() - 1);
        teamMapper.updateById(team);
    }

    @Override
    public List<ContestTeam> getTeams(Long contestId) {
        return teamMapper.selectList(
                new LambdaQueryWrapper<ContestTeam>()
                        .eq(ContestTeam::getContestId, contestId)
                        .orderByAsc(ContestTeam::getCreatedAt)
        );
    }

    @Override
    @Transactional
    public ContestSubmission submitCode(ContestSubmitDTO dto) {
        Contest contest = baseMapper.selectById(dto.getContestId());
        if (contest == null) throw new RuntimeException("比赛不存在");
        refreshContestStatus(dto.getContestId());
        contest = baseMapper.selectById(dto.getContestId());

        if (!"running".equals(contest.getStatus()) && !"frozen".equals(contest.getStatus())) {
            throw new RuntimeException("比赛未在进行中");
        }

        // 验证报名
        ContestRegistration reg = registrationMapper.selectOne(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getContestId, dto.getContestId())
                        .eq(ContestRegistration::getUserId, dto.getUserId())
                        .eq(ContestRegistration::getStatus, "registered")
        );
        if (reg == null) throw new RuntimeException("未报名该比赛");

        // 查找题目
        Problem problem = problemService.getBySlug(dto.getProblemSlug(), contest.getOjPlatform());
        if (problem == null) throw new RuntimeException("题目不存在");

        // 创建比赛提交记录
        ContestSubmission cs = new ContestSubmission();
        cs.setContestId(dto.getContestId());
        cs.setUserId(dto.getUserId());
        cs.setTeamId(reg.getTeamId());
        cs.setProblemId(problem.getId());
        cs.setLanguage(dto.getLanguage());
        cs.setCode(dto.getCode());
        cs.setStatus("Pending");
        cs.setScore(0);
        contestSubmissionMapper.insert(cs);

        // 提交到远程 OJ
        OjApiService apiService = ojApiServiceFactory.getService(contest.getOjPlatform());
        String platformLang = apiService.mapLanguage(dto.getLanguage());
        String remoteId = apiService.submitCode(
                problem.getSlug(), problem.getQuestionId(), platformLang, dto.getCode()
        );
        cs.setRemoteSubmissionId(remoteId);
        contestSubmissionMapper.updateById(cs);

        log.info("比赛提交：contestId={}, userId={}, problemSlug={}, remoteId={}",
                dto.getContestId(), dto.getUserId(), dto.getProblemSlug(), remoteId);
        return cs;
    }

    @Override
    public ContestSubmission pollResult(Long contestId, Long submissionId) {
        ContestSubmission cs = contestSubmissionMapper.selectById(submissionId);
        if (cs == null) throw new RuntimeException("提交记录不存在");
        if (!cs.getContestId().equals(contestId)) throw new RuntimeException("提交记录不属于该比赛");

        if (cs.getRemoteSubmissionId() == null || !"Pending".equals(cs.getStatus())) {
            return cs;
        }

        Contest contest = baseMapper.selectById(contestId);
        OjApiService apiService = ojApiServiceFactory.getService(contest.getOjPlatform());
        OjJudgeResult result = apiService.checkResult(cs.getRemoteSubmissionId());

        if (result.isFinished()) {
            cs.setStatus(result.getStatusMsg());
            cs.setRuntime(result.getRuntime());
            cs.setMemory(result.getMemory());
            cs.setTotalCorrect(result.getTotalCorrect());
            cs.setTotalTestcases(result.getTotalTestcases());

            // OI 赛制计算分数
            if ("oi".equals(contest.getScoringRule()) && result.getTotalTestcases() != null && result.getTotalTestcases() > 0) {
                // 查找该题在题单中的满分
                int fullScore = 100;
                if (contest.getProblemSetId() != null) {
                    List<ProblemSetItemDetailDTO> items = problemSetService.getProblemSetItems(contest.getProblemSetId());
                    for (ProblemSetItemDetailDTO item : items) {
                        if (item.getProblemId().equals(cs.getProblemId())) {
                            fullScore = item.getScore();
                            break;
                        }
                    }
                }
                int score = (int) Math.round((double) result.getTotalCorrect() / result.getTotalTestcases() * fullScore);
                cs.setScore(score);
            } else if ("Accepted".equals(result.getStatusMsg())) {
                cs.setScore(100);
            }

            contestSubmissionMapper.updateById(cs);
        }

        return cs;
    }

    @Override
    public List<ContestSubmission> getMySubmissions(Long contestId, Long userId) {
        return contestSubmissionMapper.selectList(
                new LambdaQueryWrapper<ContestSubmission>()
                        .eq(ContestSubmission::getContestId, contestId)
                        .eq(ContestSubmission::getUserId, userId)
                        .orderByDesc(ContestSubmission::getSubmittedAt)
        );
    }

    @Override
    public StandingDTO getStandings(Long contestId, Long userId) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) throw new RuntimeException("比赛不存在");

        StandingDTO standing = new StandingDTO();
        standing.setContestId(contestId);
        standing.setScoringRule(contest.getScoringRule());
        standing.setFrozen("frozen".equals(contest.getStatus()));

        // 获取题目列表
        List<ProblemSetItemDetailDTO> items = contest.getProblemSetId() != null
                ? problemSetService.getProblemSetItems(contest.getProblemSetId())
                : List.of();

        standing.setProblems(items.stream().map(item -> {
            StandingDTO.StandingProblem sp = new StandingDTO.StandingProblem();
            sp.setProblemId(item.getProblemId());
            sp.setSlug(item.getSlug());
            sp.setTitle(item.getTitle());
            sp.setFrontendId(item.getFrontendId());
            sp.setScore(item.getScore());
            return sp;
        }).collect(Collectors.toList()));

        // 获取所有提交
        List<ContestSubmission> allSubs = contestSubmissionMapper.selectList(
                new LambdaQueryWrapper<ContestSubmission>()
                        .eq(ContestSubmission::getContestId, contestId)
                        .ne(ContestSubmission::getStatus, "Pending")
                        .orderByAsc(ContestSubmission::getSubmittedAt)
        );

        // 封榜逻辑：如果当前处于封榜状态，只展示封榜前的提交
        LocalDateTime freezeTime = null;
        if ("frozen".equals(contest.getStatus()) && contest.getFreezeMinutes() > 0) {
            freezeTime = contest.getEndTime().minusMinutes(contest.getFreezeMinutes());
        }

        // 按参赛者分组计算成绩
        boolean isTeamContest = "team".equals(contest.getContestType());
        Map<Long, List<ContestSubmission>> grouped;
        if (isTeamContest) {
            grouped = allSubs.stream()
                    .filter(s -> s.getTeamId() != null)
                    .collect(Collectors.groupingBy(ContestSubmission::getTeamId));
        } else {
            grouped = allSubs.stream()
                    .collect(Collectors.groupingBy(ContestSubmission::getUserId));
        }

        List<StandingDTO.StandingRow> rows = new ArrayList<>();
        LocalDateTime finalFreezeTime = freezeTime;

        for (Map.Entry<Long, List<ContestSubmission>> entry : grouped.entrySet()) {
            StandingDTO.StandingRow row = new StandingDTO.StandingRow();

            if (isTeamContest) {
                ContestTeam team = teamMapper.selectById(entry.getKey());
                row.setTeamId(entry.getKey());
                row.setTeamName(team != null ? team.getTeamName() : "未知队伍");
            } else {
                User user = userMapper.selectById(entry.getKey());
                row.setUserId(entry.getKey());
                row.setUsername(user != null ? user.getUsername() : "未知用户");
            }

            List<ContestSubmission> subs = entry.getValue();
            List<StandingDTO.ProblemResult> problemResults = new ArrayList<>();
            int solvedCount = 0;
            int totalScore = 0;
            long totalPenalty = 0;

            for (ProblemSetItemDetailDTO item : items) {
                List<ContestSubmission> problemSubs = subs.stream()
                        .filter(s -> s.getProblemId().equals(item.getProblemId()))
                        .collect(Collectors.toList());

                StandingDTO.ProblemResult pr = new StandingDTO.ProblemResult();
                pr.setProblemId(item.getProblemId());
                pr.setAccepted(false);
                pr.setAttempts(0);
                pr.setScore(0);
                pr.setFrozen(false);

                // 检查是否有封榜后的提交
                boolean hasFrozenSubs = false;
                if (finalFreezeTime != null) {
                    hasFrozenSubs = problemSubs.stream()
                            .anyMatch(s -> s.getSubmittedAt().isAfter(finalFreezeTime));
                }

                // 只处理封榜前的提交（如果有封榜）
                List<ContestSubmission> visibleSubs = problemSubs;
                if (finalFreezeTime != null) {
                    visibleSubs = problemSubs.stream()
                            .filter(s -> !s.getSubmittedAt().isAfter(finalFreezeTime))
                            .collect(Collectors.toList());
                }

                if (hasFrozenSubs && finalFreezeTime != null) {
                    pr.setFrozen(true);
                    pr.setAttempts(visibleSubs.size() + (int) problemSubs.stream()
                            .filter(s -> s.getSubmittedAt().isAfter(finalFreezeTime)).count());
                }

                switch (contest.getScoringRule()) {
                    case "acm" -> calculateAcm(pr, visibleSubs, contest, item);
                    case "oi" -> calculateOi(pr, visibleSubs, item);
                    case "cf" -> calculateCf(pr, visibleSubs, contest, item);
                }

                if (pr.getAccepted()) solvedCount++;
                totalScore += pr.getScore();
                if (pr.getAccepted() && pr.getFirstAcTimeSeconds() != null) {
                    totalPenalty += pr.getFirstAcTimeSeconds() + (long)(pr.getAttempts() - 1) * contest.getPenaltyTime() * 60;
                }

                problemResults.add(pr);
            }

            row.setSolvedCount(solvedCount);
            row.setTotalScore(totalScore);
            row.setTotalPenalty(totalPenalty);
            row.setProblemResults(problemResults);
            rows.add(row);
        }

        // 排序
        switch (contest.getScoringRule()) {
            case "acm" -> rows.sort(Comparator
                    .comparingInt(StandingDTO.StandingRow::getSolvedCount).reversed()
                    .thenComparingLong(StandingDTO.StandingRow::getTotalPenalty));
            case "oi", "cf" -> rows.sort(Comparator
                    .comparingInt(StandingDTO.StandingRow::getTotalScore).reversed()
                    .thenComparingLong(StandingDTO.StandingRow::getTotalPenalty));
        }

        // 设置排名
        for (int i = 0; i < rows.size(); i++) {
            rows.get(i).setRank(i + 1);
        }

        standing.setRows(rows);
        return standing;
    }

    @Override
    public void unfreezeStandings(Long contestId, Long userId) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) throw new RuntimeException("比赛不存在");
        if (!contest.getCreatorId().equals(userId)) throw new RuntimeException("只有创建者才能解封");
        if (!"frozen".equals(contest.getStatus()) && !"ended".equals(contest.getStatus())) {
            throw new RuntimeException("比赛未处于封榜或已结束状态");
        }

        contest.setStatus("ended");
        contest.setFreezeMinutes(0);
        baseMapper.updateById(contest);
        log.info("比赛解封：id={}", contestId);
    }

    @Override
    public List<ProblemSetItemDetailDTO> getContestProblems(Long contestId, Long userId) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) throw new RuntimeException("比赛不存在");
        refreshContestStatus(contestId);
        contest = baseMapper.selectById(contestId);

        // 只有比赛进行中/封榜/结束后才能查看题目（或者是创建者）
        boolean isCreator = contest.getCreatorId().equals(userId);
        boolean canView = "running".equals(contest.getStatus())
                || "frozen".equals(contest.getStatus())
                || "ended".equals(contest.getStatus())
                || "archived".equals(contest.getStatus());

        if (!isCreator && !canView) {
            throw new RuntimeException("比赛未开始，暂时无法查看题目");
        }

        if (contest.getProblemSetId() == null) return List.of();
        return problemSetService.getProblemSetItems(contest.getProblemSetId());
    }

    @Override
    public void refreshContestStatus(Long contestId) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) return;

        LocalDateTime now = LocalDateTime.now();
        String currentStatus = contest.getStatus();

        // 不处理草稿和已归档状态
        if ("draft".equals(currentStatus) || "archived".equals(currentStatus)) return;

        String newStatus = currentStatus;

        if ("registering".equals(currentStatus) && now.isAfter(contest.getStartTime())) {
            newStatus = "running";
        }

        if ("running".equals(currentStatus)) {
            if (contest.getFreezeMinutes() > 0) {
                LocalDateTime freezeTime = contest.getEndTime().minusMinutes(contest.getFreezeMinutes());
                if (now.isAfter(freezeTime) && now.isBefore(contest.getEndTime())) {
                    newStatus = "frozen";
                }
            }
            if (now.isAfter(contest.getEndTime())) {
                newStatus = "ended";
            }
        }

        if ("frozen".equals(currentStatus) && now.isAfter(contest.getEndTime())) {
            newStatus = "ended";
        }

        if (!newStatus.equals(currentStatus)) {
            contest.setStatus(newStatus);
            baseMapper.updateById(contest);
            log.info("比赛状态变更：id={}, {} → {}", contestId, currentStatus, newStatus);
        }
    }

    // ==================== 私有辅助方法 ====================

    private ContestDetailDTO toContestDetailDTO(Contest contest, Long userId) {
        ContestDetailDTO dto = new ContestDetailDTO();
        dto.setId(contest.getId());
        dto.setTitle(contest.getTitle());
        dto.setDescription(contest.getDescription());
        dto.setContestType(contest.getContestType());
        dto.setStatus(contest.getStatus());
        dto.setStartTime(contest.getStartTime());
        dto.setEndTime(contest.getEndTime());
        dto.setDurationMinutes(contest.getDurationMinutes());
        dto.setFreezeMinutes(contest.getFreezeMinutes());
        dto.setScoringRule(contest.getScoringRule());
        dto.setPenaltyTime(contest.getPenaltyTime());
        dto.setMaxParticipants(contest.getMaxParticipants());
        dto.setMaxTeamSize(contest.getMaxTeamSize());
        dto.setIsPublic(contest.getIsPublic());
        dto.setOjPlatform(contest.getOjPlatform());
        dto.setCreatedAt(contest.getCreatedAt());
        dto.setIsCreator(contest.getCreatorId().equals(userId));

        // 创建者用户名
        User creator = userMapper.selectById(contest.getCreatorId());
        dto.setCreatorName(creator != null ? creator.getUsername() : "未知");

        // 报名人数
        Long regCount = registrationMapper.selectCount(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getContestId, contest.getId())
                        .eq(ContestRegistration::getStatus, "registered")
        );
        dto.setRegisteredCount(regCount.intValue());

        // 队伍数
        Long tCount = teamMapper.selectCount(
                new LambdaQueryWrapper<ContestTeam>()
                        .eq(ContestTeam::getContestId, contest.getId())
        );
        dto.setTeamCount(tCount.intValue());

        // 题目数
        if (contest.getProblemSetId() != null) {
            ProblemSet ps = problemSetService.getById(contest.getProblemSetId());
            dto.setProblemCount(ps != null ? ps.getProblemCount() : 0);
        } else {
            dto.setProblemCount(0);
        }

        // 当前用户是否已报名
        if (userId != null) {
            Long myReg = registrationMapper.selectCount(
                    new LambdaQueryWrapper<ContestRegistration>()
                            .eq(ContestRegistration::getContestId, contest.getId())
                            .eq(ContestRegistration::getUserId, userId)
                            .eq(ContestRegistration::getStatus, "registered")
            );
            dto.setRegistered(myReg > 0);
        } else {
            dto.setRegistered(false);
        }

        return dto;
    }

    /**
     * ACM 罚时制计算
     */
    private void calculateAcm(StandingDTO.ProblemResult pr, List<ContestSubmission> subs,
                               Contest contest, ProblemSetItemDetailDTO item) {
        int attempts = 0;
        for (ContestSubmission s : subs) {
            attempts++;
            if ("Accepted".equals(s.getStatus())) {
                pr.setAccepted(true);
                pr.setAttempts(attempts);
                long acTime = Duration.between(contest.getStartTime(), s.getSubmittedAt()).getSeconds();
                pr.setFirstAcTimeSeconds(acTime);
                pr.setScore(item.getScore());
                return;
            }
        }
        pr.setAttempts(attempts);
    }

    /**
     * OI 分数制计算（取最后一次提交的分数）
     */
    private void calculateOi(StandingDTO.ProblemResult pr, List<ContestSubmission> subs,
                              ProblemSetItemDetailDTO item) {
        if (subs.isEmpty()) return;
        pr.setAttempts(subs.size());

        // 取最后一次提交
        ContestSubmission lastSub = subs.get(subs.size() - 1);
        pr.setScore(lastSub.getScore());
        pr.setAccepted("Accepted".equals(lastSub.getStatus()));
    }

    /**
     * CF 风格计算（按时间衰减分数）
     */
    private void calculateCf(StandingDTO.ProblemResult pr, List<ContestSubmission> subs,
                              Contest contest, ProblemSetItemDetailDTO item) {
        int attempts = 0;
        for (ContestSubmission s : subs) {
            attempts++;
            if ("Accepted".equals(s.getStatus())) {
                pr.setAccepted(true);
                pr.setAttempts(attempts);
                long acTimeMinutes = Duration.between(contest.getStartTime(), s.getSubmittedAt()).toMinutes();
                pr.setFirstAcTimeSeconds(acTimeMinutes * 60);

                // CF 分数公式：max(3*满分/10, 满分 - 满分/250*t - 50*(尝试-1))
                int maxScore = item.getScore();
                int score = (int) Math.max(
                        3.0 * maxScore / 10,
                        maxScore - (double) maxScore / 250 * acTimeMinutes - 50.0 * (attempts - 1)
                );
                pr.setScore(score);
                return;
            }
        }
        pr.setAttempts(attempts);
    }
}
