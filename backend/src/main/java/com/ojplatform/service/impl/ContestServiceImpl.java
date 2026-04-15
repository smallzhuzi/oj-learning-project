package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ojplatform.dto.*;
import com.ojplatform.entity.*;
import com.ojplatform.mapper.*;
import com.ojplatform.service.*;
import com.fasterxml.jackson.core.type.TypeReference;
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
 * 比赛相关业务实现。
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
    private ProblemSetService problemSetService;
    @Autowired
    private ProblemSetItemMapper problemSetItemMapper;
    @Autowired
    private ProblemService problemService;
    @Autowired
    private ContestJudgeQueueService contestJudgeQueueService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private com.ojplatform.mapper.TeamMapper independentTeamMapper;
    @Autowired
    private com.ojplatform.mapper.TeamMemberMapper independentTeamMemberMapper;
    @Autowired
    private com.ojplatform.mapper.ContestTeamParticipantMapper participantMapper;
    @Autowired
    private ContestStandingSnapshotService contestStandingSnapshotService;

    /**
     * 创建比赛草稿，并在需要时直接发布。
     */
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
        contest.setMinTeamSize(dto.getMinTeamSize());
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

        // 2. 草稿阶段：只把题目列表存为 JSON，不创建题单
        saveDraftProblems(contest, dto);

        baseMapper.insert(contest);
        log.info("创建比赛草稿：id={}, title={}", contest.getId(), contest.getTitle());

        // 3. 如果要求直接发布，则立即发布
        if (Boolean.TRUE.equals(dto.getPublish())) {
            publishContest(contest.getId(), dto.getUserId());
            contest = baseMapper.selectById(contest.getId());
        }

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
        if (dto.getMinTeamSize() != null) contest.setMinTeamSize(dto.getMinTeamSize());
        if (dto.getScoringRule() != null) contest.setScoringRule(dto.getScoringRule());
        if (dto.getPenaltyTime() != null) contest.setPenaltyTime(dto.getPenaltyTime());
        if (dto.getIsPublic() != null) contest.setIsPublic(dto.getIsPublic());
        if (dto.getPassword() != null) contest.setPassword(dto.getPassword().isBlank() ? null : dto.getPassword());
        if (dto.getAllowLanguage() != null) {
            try { contest.setAllowLanguage(objectMapper.writeValueAsString(dto.getAllowLanguage())); } catch (Exception ignored) {}
        }

        // 草稿阶段：更新暂存题目列表（不创建题单）
        saveDraftProblems(contest, dto);

        baseMapper.updateById(contest);
        log.info("更新比赛草稿：id={}", contestId);

        // 如果要求直接发布，则立即发布
        if (Boolean.TRUE.equals(dto.getPublish())) {
            publishContest(contestId, dto.getUserId());
        }
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
    @Transactional
    public void publishContest(Long contestId, Long userId) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) throw new RuntimeException("比赛不存在");
        if (!contest.getCreatorId().equals(userId)) throw new RuntimeException("无权操作");
        if (!"draft".equals(contest.getStatus())) throw new RuntimeException("只有草稿状态的比赛才能发布");

        // 从 draftProblems 解析题目列表
        List<CreateContestDTO.ContestProblemItem> draftItems = parseDraftProblems(contest.getDraftProblems());
        if (draftItems.isEmpty()) {
            throw new RuntimeException("请先设置比赛题目");
        }

        // 创建正式题单
        CreateProblemSetDTO psDto = new CreateProblemSetDTO();
        psDto.setUserId(userId);
        psDto.setTitle(contest.getTitle() + " - 比赛题单");
        psDto.setOjPlatform(contest.getOjPlatform() != null ? contest.getOjPlatform() : "leetcode");
        List<CreateProblemSetDTO.ProblemItem> items = draftItems.stream().map(cp -> {
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
        contest.setDraftProblems(null);
        contest.setStatus("registering");
        baseMapper.updateById(contest);
        log.info("比赛已发布：id={}, 题目数={}", contestId, draftItems.size());
    }

    @Override
    @Transactional
    public void registerContest(Long contestId, Long userId, String password, Long teamId, java.util.List<Long> memberUserIds) {
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

        // 组队赛：用独立队伍报名 + 选出场成员
        if ("team".equals(contest.getContestType())) {
            if (teamId == null) throw new RuntimeException("组队赛需要选择一个队伍来报名");
            if (memberUserIds == null || memberUserIds.isEmpty()) throw new RuntimeException("请选择出场成员");

            com.ojplatform.entity.Team team = independentTeamMapper.selectById(teamId);
            if (team == null) throw new RuntimeException("队伍不存在");
            if (!team.getCaptainId().equals(userId)) throw new RuntimeException("只有队长可以代表队伍报名");

            // 校验出场人数
            int minSize = contest.getMinTeamSize() != null ? contest.getMinTeamSize() : 1;
            int maxSize = contest.getMaxTeamSize() != null ? contest.getMaxTeamSize() : 99;
            if (memberUserIds.size() < minSize) {
                throw new RuntimeException("出场人数不足，至少需要 " + minSize + " 人");
            }
            if (memberUserIds.size() > maxSize) {
                throw new RuntimeException("出场人数超出限制，最多 " + maxSize + " 人");
            }

            // 校验选中的成员是否都在队伍中
            for (Long memberId : memberUserIds) {
                Long exists = independentTeamMemberMapper.selectCount(
                        new LambdaQueryWrapper<com.ojplatform.entity.TeamMember>()
                                .eq(com.ojplatform.entity.TeamMember::getTeamId, teamId)
                                .eq(com.ojplatform.entity.TeamMember::getUserId, memberId)
                );
                if (exists == 0) throw new RuntimeException("成员 " + memberId + " 不在队伍中");
            }

            // 检查队伍是否已报名
            Long teamExists = registrationMapper.selectCount(
                    new LambdaQueryWrapper<ContestRegistration>()
                            .eq(ContestRegistration::getContestId, contestId)
                            .eq(ContestRegistration::getTeamId, teamId)
                            .eq(ContestRegistration::getStatus, "registered")
            );
            if (teamExists > 0) throw new RuntimeException("该队伍已报名该比赛");

            // 检查名额限制
            if (contest.getMaxParticipants() > 0) {
                Long count = registrationMapper.selectCount(
                        new LambdaQueryWrapper<ContestRegistration>()
                                .eq(ContestRegistration::getContestId, contestId)
                                .eq(ContestRegistration::getStatus, "registered")
                );
                if (count >= contest.getMaxParticipants()) throw new RuntimeException("报名名额已满");
            }

            // 写入报名记录
            upsertRegistration(contestId, userId, teamId);

            participantMapper.delete(
                    new LambdaQueryWrapper<com.ojplatform.entity.ContestTeamParticipant>()
                            .eq(com.ojplatform.entity.ContestTeamParticipant::getContestId, contestId)
                            .eq(com.ojplatform.entity.ContestTeamParticipant::getTeamId, teamId)
            );

            // 写入出场成员
            for (Long memberId : memberUserIds) {
                com.ojplatform.entity.ContestTeamParticipant p = new com.ojplatform.entity.ContestTeamParticipant();
                p.setContestId(contestId);
                p.setTeamId(teamId);
                p.setUserId(memberId);
                participantMapper.insert(p);
            }

            log.info("队伍报名比赛：contestId={}, teamId={}, 出场{}人", contestId, teamId, memberUserIds.size());
            return;
        }

        // 个人赛报名
        // 检查名额限制
        if (contest.getMaxParticipants() > 0) {
            Long count = registrationMapper.selectCount(
                    new LambdaQueryWrapper<ContestRegistration>()
                            .eq(ContestRegistration::getContestId, contestId)
                            .eq(ContestRegistration::getStatus, "registered")
            );
            if (count >= contest.getMaxParticipants()) throw new RuntimeException("报名名额已满");
        }

        Long exists = registrationMapper.selectCount(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getContestId, contestId)
                        .eq(ContestRegistration::getUserId, userId)
                        .eq(ContestRegistration::getStatus, "registered")
        );
        if (exists > 0) throw new RuntimeException("已报名该比赛");

        upsertRegistration(contestId, userId, null);
        log.info("用户报名比赛：userId={}, contestId={}", userId, contestId);
    }

    @Override
    @Transactional
    public void cancelRegistration(Long contestId, Long userId) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) throw new RuntimeException("比赛不存在");

        if ("team".equals(contest.getContestType())) {
            // 查找该用户作为报名人的队伍报名记录
            ContestRegistration teamReg = registrationMapper.selectOne(
                    new LambdaQueryWrapper<ContestRegistration>()
                            .eq(ContestRegistration::getContestId, contestId)
                            .eq(ContestRegistration::getUserId, userId)
                            .eq(ContestRegistration::getStatus, "registered")
                            .isNotNull(ContestRegistration::getTeamId)
            );
            if (teamReg == null) throw new RuntimeException("你的队伍尚未报名该比赛");

            // 验证是否为队长
            com.ojplatform.entity.Team team = independentTeamMapper.selectById(teamReg.getTeamId());
            if (team == null || !team.getCaptainId().equals(userId)) {
                throw new RuntimeException("只有队长可以取消队伍报名");
            }

            teamReg.setStatus("cancelled");
            registrationMapper.updateById(teamReg);
            participantMapper.delete(
                    new LambdaQueryWrapper<com.ojplatform.entity.ContestTeamParticipant>()
                            .eq(com.ojplatform.entity.ContestTeamParticipant::getContestId, contestId)
                            .eq(com.ojplatform.entity.ContestTeamParticipant::getTeamId, teamReg.getTeamId())
            );
            return;
        }

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

        // 检查用户是否已在该比赛的其他队伍中
        ContestTeam existingTeam = findUserTeam(contestId, dto.getUserId());
        if (existingTeam != null) {
            throw new RuntimeException("你已在该比赛的其他队伍中，请先退出后再创建");
        }

        ContestTeam team = new ContestTeam();
        team.setContestId(contestId);
        team.setTeamName(dto.getTeamName());
        team.setDescription(dto.getDescription());
        team.setCaptainId(dto.getUserId());
        team.setMemberCount(1);
        teamMapper.insert(team);

        // 队长加入队伍
        ContestTeamMember member = new ContestTeamMember();
        member.setTeamId(team.getId());
        member.setUserId(dto.getUserId());
        member.setRole("captain");
        teamMemberMapper.insert(member);

        log.info("创建队伍：contestId={}, teamName={}", contestId, dto.getTeamName());
        return team;
    }

    @Override
    @Transactional
    public void leaveTeam(Long contestId, Long teamId, Long userId) {
        ContestTeam team = teamMapper.selectById(teamId);
        if (team == null) throw new RuntimeException("队伍不存在");
        if (team.getCaptainId().equals(userId)) throw new RuntimeException("队长不能直接退出队伍，请先转让队长或解散队伍");

        teamMemberMapper.delete(
                new LambdaQueryWrapper<ContestTeamMember>()
                        .eq(ContestTeamMember::getTeamId, teamId)
                        .eq(ContestTeamMember::getUserId, userId)
        );
        team.setMemberCount(team.getMemberCount() - 1);
        teamMapper.updateById(team);
        clearRegistrationTeam(contestId, userId);
    }

    @Override
    public List<ContestTeamLobbyDTO> getTeams(Long contestId) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) {
            throw new RuntimeException("比赛不存在");
        }

        return teamMapper.selectList(
                new LambdaQueryWrapper<ContestTeam>()
                        .eq(ContestTeam::getContestId, contestId)
                        .orderByAsc(ContestTeam::getCreatedAt)
        ).stream().map(team -> {
            ContestTeamLobbyDTO dto = new ContestTeamLobbyDTO();
            dto.setId(team.getId());
            dto.setContestId(team.getContestId());
            dto.setTeamName(team.getTeamName());
            dto.setDescription(team.getDescription());
            dto.setCaptainId(team.getCaptainId());
            dto.setMemberCount(team.getMemberCount());
            dto.setRegistered(isTeamRegistered(contestId, team.getId()));
            dto.setCreatedAt(team.getCreatedAt());

            User captain = userMapper.selectById(team.getCaptainId());
            dto.setCaptainName(captain != null ? captain.getUsername() : "未知队长");

            // 填充成员用户名列表
            List<ContestTeamMember> members = teamMemberMapper.selectList(
                    new LambdaQueryWrapper<ContestTeamMember>()
                            .eq(ContestTeamMember::getTeamId, team.getId())
                            .orderByAsc(ContestTeamMember::getJoinedAt)
            );
            List<String> memberNames = members.stream().map(m -> {
                User u = userMapper.selectById(m.getUserId());
                return u != null ? u.getUsername() : "未知用户";
            }).collect(Collectors.toList());
            dto.setMemberNames(memberNames);

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public ContestTeamDetailDTO getMyTeam(Long contestId, Long userId) {
        ContestTeam team = findUserTeam(contestId, userId);
        if (team == null) {
            return null;
        }
        return buildTeamDetail(contestId, team.getId(), userId);
    }

    @Override
    public List<MyTeamSummaryDTO> getMyTeams(Long userId) {
        List<ContestTeamMember> memberships = teamMemberMapper.selectList(
                new LambdaQueryWrapper<ContestTeamMember>()
                        .eq(ContestTeamMember::getUserId, userId)
                        .orderByDesc(ContestTeamMember::getJoinedAt)
        );
        if (memberships.isEmpty()) {
            return List.of();
        }

        List<MyTeamSummaryDTO> summaries = new ArrayList<>();
        for (ContestTeamMember membership : memberships) {
            ContestTeam team = teamMapper.selectById(membership.getTeamId());
            if (team == null) {
                continue;
            }

            Contest contest = baseMapper.selectById(team.getContestId());
            if (contest == null || !"team".equals(contest.getContestType())) {
                continue;
            }
            refreshContestStatus(contest.getId());
            contest = baseMapper.selectById(contest.getId());

            MyTeamSummaryDTO dto = new MyTeamSummaryDTO();
            dto.setContestId(contest.getId());
            dto.setContestTitle(contest.getTitle());
            dto.setContestStatus(contest.getStatus());
            dto.setTeamId(team.getId());
            dto.setTeamName(team.getTeamName());
            dto.setCaptain(team.getCaptainId().equals(userId));
            dto.setRegistered(isTeamRegistered(contest.getId(), team.getId()));
            dto.setMemberCount(team.getMemberCount());
            dto.setMaxTeamSize(contest.getMaxTeamSize());
            dto.setStartTime(contest.getStartTime());

            User captain = userMapper.selectById(team.getCaptainId());
            dto.setCaptainName(captain != null ? captain.getUsername() : "未知队长");
            summaries.add(dto);
        }
        summaries.sort(Comparator.comparing(MyTeamSummaryDTO::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())));
        return summaries;
    }

    @Override
    @Transactional
    public ContestTeamDetailDTO updateTeam(Long contestId, Long teamId, UpdateTeamDTO dto) {
        ContestTeam team = requireCaptainAndEditable(contestId, teamId, dto.getUserId());
        team.setTeamName(dto.getTeamName().trim());
        if (dto.getDescription() != null) {
            team.setDescription(dto.getDescription().trim().isEmpty() ? null : dto.getDescription().trim());
        }
        teamMapper.updateById(team);
        return buildTeamDetail(contestId, teamId, dto.getUserId());
    }

    @Override
    @Transactional
    public void transferCaptain(Long contestId, Long teamId, TransferCaptainDTO dto) {
        ContestTeam team = requireCaptainAndEditable(contestId, teamId, dto.getUserId());
        ContestTeamMember targetMember = teamMemberMapper.selectOne(
                new LambdaQueryWrapper<ContestTeamMember>()
                        .eq(ContestTeamMember::getTeamId, teamId)
                        .eq(ContestTeamMember::getUserId, dto.getTargetUserId())
        );
        if (targetMember == null) {
            throw new RuntimeException("目标成员不在队伍中");
        }
        if (dto.getTargetUserId().equals(dto.getUserId())) {
            throw new RuntimeException("无需转让给自己");
        }

        ContestTeamMember captainMember = teamMemberMapper.selectOne(
                new LambdaQueryWrapper<ContestTeamMember>()
                        .eq(ContestTeamMember::getTeamId, teamId)
                        .eq(ContestTeamMember::getUserId, dto.getUserId())
        );
        captainMember.setRole("member");
        targetMember.setRole("captain");
        teamMemberMapper.updateById(captainMember);
        teamMemberMapper.updateById(targetMember);

        team.setCaptainId(dto.getTargetUserId());
        teamMapper.updateById(team);

        ContestRegistration teamReg = registrationMapper.selectOne(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getContestId, contestId)
                        .eq(ContestRegistration::getTeamId, teamId)
                        .eq(ContestRegistration::getStatus, "registered")
        );
        if (teamReg != null) {
            teamReg.setUserId(dto.getTargetUserId());
            registrationMapper.updateById(teamReg);
        }
    }

    @Override
    @Transactional
    public void removeTeamMember(Long contestId, Long teamId, Long operatorUserId, Long targetUserId) {
        ContestTeam team = requireCaptainAndEditable(contestId, teamId, operatorUserId);
        if (operatorUserId.equals(targetUserId)) {
            throw new RuntimeException("队长不能移除自己，请直接解散队伍或转让队长");
        }

        ContestTeamMember targetMember = teamMemberMapper.selectOne(
                new LambdaQueryWrapper<ContestTeamMember>()
                        .eq(ContestTeamMember::getTeamId, teamId)
                        .eq(ContestTeamMember::getUserId, targetUserId)
        );
        if (targetMember == null) {
            throw new RuntimeException("目标成员不在队伍中");
        }

        teamMemberMapper.deleteById(targetMember.getId());
        team.setMemberCount(Math.max(0, team.getMemberCount() - 1));
        teamMapper.updateById(team);
        clearRegistrationTeam(contestId, targetUserId);
    }

    @Override
    @Transactional
    public void dissolveTeam(Long contestId, Long teamId, Long userId) {
        requireCaptainAndEditable(contestId, teamId, userId);

        ContestRegistration teamReg = registrationMapper.selectOne(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getContestId, contestId)
                        .eq(ContestRegistration::getTeamId, teamId)
                        .eq(ContestRegistration::getStatus, "registered")
        );
        if (teamReg != null) {
            teamReg.setStatus("cancelled");
            registrationMapper.updateById(teamReg);
        }

        teamMemberMapper.delete(new LambdaQueryWrapper<ContestTeamMember>().eq(ContestTeamMember::getTeamId, teamId));
        teamMapper.deleteById(teamId);
    }

    /**
     * 校验参赛资格后创建比赛提交，并把判题任务送入队列。
     */
    @Override
    public ContestSubmission submitCode(ContestSubmitDTO dto) {
        Contest contest = baseMapper.selectById(dto.getContestId());
        if (contest == null) throw new RuntimeException("比赛不存在");
        refreshContestStatus(dto.getContestId());
        contest = baseMapper.selectById(dto.getContestId());

        if (!"running".equals(contest.getStatus()) && !"frozen".equals(contest.getStatus())) {
            throw new RuntimeException("比赛未在进行中");
        }

        ContestRegistration reg;
        if ("team".equals(contest.getContestType())) {
            ContestTeam myTeam = findUserTeam(dto.getContestId(), dto.getUserId());
            if (myTeam == null) throw new RuntimeException("你当前不在该比赛的任何队伍中");

            reg = registrationMapper.selectOne(
                    new LambdaQueryWrapper<ContestRegistration>()
                            .eq(ContestRegistration::getContestId, dto.getContestId())
                            .eq(ContestRegistration::getTeamId, myTeam.getId())
                            .eq(ContestRegistration::getStatus, "registered")
            );
            if (reg == null) throw new RuntimeException("你的队伍尚未完成比赛报名");
        } else {
            reg = registrationMapper.selectOne(
                    new LambdaQueryWrapper<ContestRegistration>()
                            .eq(ContestRegistration::getContestId, dto.getContestId())
                            .eq(ContestRegistration::getUserId, dto.getUserId())
                            .eq(ContestRegistration::getStatus, "registered")
            );
            if (reg == null) throw new RuntimeException("未报名该比赛");
        }

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
        try {
            contestJudgeQueueService.enqueueSubmit(cs.getId());
        } catch (RuntimeException e) {
            cs.setStatus("Submit Failed");
            contestSubmissionMapper.updateById(cs);
            throw new RuntimeException("提交任务入队失败，请稍后重试");
        }

        log.info("Contest submission queued. contestId={}, submissionId={}, userId={}, problemSlug={}",
                dto.getContestId(), cs.getId(), dto.getUserId(), dto.getProblemSlug());
        return cs;
    }

    @Override
    public ContestSubmission pollResult(Long contestId, Long submissionId) {
        ContestSubmission cs = contestSubmissionMapper.selectById(submissionId);
        if (cs == null) throw new RuntimeException("提交记录不存在");
        if (!cs.getContestId().equals(contestId)) throw new RuntimeException("提交记录不属于该比赛");
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

    /**
     * 比赛榜单统一从快照服务读取，避免每次请求全量扫描提交记录。
     */
    @Override
    public StandingDTO getStandings(Long contestId, Long userId) {
        return contestStandingSnapshotService.getStanding(contestId);
        /*
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
        */
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
        contestStandingSnapshotService.rebuildContestSnapshot(contestId, false);
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

    /**
     * 根据当前时间推进比赛状态，并在封榜或结束时刷新榜单快照。
     */
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
            if ("frozen".equals(newStatus)) {
                contestStandingSnapshotService.rebuildContestSnapshot(contestId, true);
            } else if ("ended".equals(newStatus)) {
                contestStandingSnapshotService.rebuildContestSnapshot(contestId, false);
            }
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
        dto.setMinTeamSize(contest.getMinTeamSize());
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
            // 草稿阶段：从 draftProblems 计算题目数
            List<CreateContestDTO.ContestProblemItem> draftItems = parseDraftProblems(contest.getDraftProblems());
            dto.setProblemCount(draftItems.size());
        }

        // 当前用户是否已报名
        if (userId != null) {
            if ("team".equals(contest.getContestType())) {
                ContestTeam myTeam = findUserTeam(contest.getId(), userId);
                dto.setRegistered(myTeam != null && isTeamRegistered(contest.getId(), myTeam.getId()));
            } else {
                Long myReg = registrationMapper.selectCount(
                        new LambdaQueryWrapper<ContestRegistration>()
                                .eq(ContestRegistration::getContestId, contest.getId())
                                .eq(ContestRegistration::getUserId, userId)
                                .eq(ContestRegistration::getStatus, "registered")
                );
                dto.setRegistered(myReg > 0);
            }
        } else {
            dto.setRegistered(false);
        }

        // 草稿题目（仅创建者 + 草稿态可见）
        if ("draft".equals(contest.getStatus()) && contest.getCreatorId().equals(userId)) {
            dto.setDraftProblems(contest.getDraftProblems());
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

    private ContestTeam findUserTeam(Long contestId, Long userId) {
        ContestTeamMember membership = teamMemberMapper.selectOne(
                new LambdaQueryWrapper<ContestTeamMember>()
                        .inSql(ContestTeamMember::getTeamId, "select id from contest_teams where contest_id = " + contestId)
                        .eq(ContestTeamMember::getUserId, userId)
                        .last("limit 1")
        );
        return membership == null ? null : teamMapper.selectById(membership.getTeamId());
    }

    private boolean isTeamRegistered(Long contestId, Long teamId) {
        return registrationMapper.selectCount(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getContestId, contestId)
                        .eq(ContestRegistration::getTeamId, teamId)
                        .eq(ContestRegistration::getStatus, "registered")
        ) > 0;
    }

    private ContestRegistration upsertRegistration(Long contestId, Long userId, Long teamId) {
        ContestRegistration existing = registrationMapper.selectOne(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getContestId, contestId)
                        .eq(ContestRegistration::getUserId, userId)
                        .last("limit 1")
        );

        if (existing != null) {
            existing.setTeamId(teamId);
            existing.setStatus("registered");
            existing.setRegisteredAt(LocalDateTime.now());
            registrationMapper.updateById(existing);
            return existing;
        }

        ContestRegistration reg = new ContestRegistration();
        reg.setContestId(contestId);
        reg.setUserId(userId);
        reg.setTeamId(teamId);
        reg.setStatus("registered");
        reg.setRegisteredAt(LocalDateTime.now());
        registrationMapper.insert(reg);
        return reg;
    }

    private ContestTeam requireCaptainAndEditable(Long contestId, Long teamId, Long userId) {
        Contest contest = baseMapper.selectById(contestId);
        if (contest == null) {
            throw new RuntimeException("比赛不存在");
        }
        refreshContestStatus(contestId);
        contest = baseMapper.selectById(contestId);
        if (!"registering".equals(contest.getStatus())) {
            throw new RuntimeException("比赛开始后不允许调整队伍");
        }

        ContestTeam team = teamMapper.selectById(teamId);
        if (team == null || !team.getContestId().equals(contestId)) {
            throw new RuntimeException("队伍不存在");
        }
        if (!team.getCaptainId().equals(userId)) {
            throw new RuntimeException("仅队长可执行该操作");
        }
        return team;
    }

    private ContestTeamDetailDTO buildTeamDetail(Long contestId, Long teamId, Long currentUserId) {
        ContestTeam team = teamMapper.selectById(teamId);
        if (team == null || !team.getContestId().equals(contestId)) {
            throw new RuntimeException("队伍不存在");
        }

        List<ContestTeamMember> members = teamMemberMapper.selectList(
                new LambdaQueryWrapper<ContestTeamMember>()
                        .eq(ContestTeamMember::getTeamId, teamId)
                        .orderByAsc(ContestTeamMember::getJoinedAt)
        );

        ContestTeamDetailDTO dto = new ContestTeamDetailDTO();
        dto.setId(team.getId());
        dto.setContestId(team.getContestId());
        dto.setTeamName(team.getTeamName());
        dto.setDescription(team.getDescription());
        dto.setCaptainId(team.getCaptainId());
        dto.setMemberCount(team.getMemberCount());
        dto.setCreatedAt(team.getCreatedAt());
        dto.setCaptain(team.getCaptainId().equals(currentUserId));

        User captain = userMapper.selectById(team.getCaptainId());
        dto.setCaptainName(captain != null ? captain.getUsername() : "未知用户");

        List<ContestTeamMemberDTO> memberDTOs = new ArrayList<>();
        for (ContestTeamMember member : members) {
            ContestTeamMemberDTO memberDTO = new ContestTeamMemberDTO();
            memberDTO.setUserId(member.getUserId());
            memberDTO.setRole(member.getRole());
            memberDTO.setJoinedAt(member.getJoinedAt());
            User user = userMapper.selectById(member.getUserId());
            memberDTO.setUsername(user != null ? user.getUsername() : "未知用户");
            if (member.getUserId().equals(currentUserId)) {
                dto.setMyRole(member.getRole());
            }
            memberDTOs.add(memberDTO);
        }
        dto.setMembers(memberDTOs);
        return dto;
    }

    private void clearRegistrationTeam(Long contestId, Long userId) {
        ContestRegistration reg = registrationMapper.selectOne(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getContestId, contestId)
                        .eq(ContestRegistration::getUserId, userId)
                        .eq(ContestRegistration::getStatus, "registered")
        );
        if (reg != null) {
            reg.setTeamId(null);
            registrationMapper.updateById(reg);
        }
    }

    /**
     * 将 DTO 中的题目列表序列化为 JSON 存入 draftProblems 字段
     */
    private void saveDraftProblems(Contest contest, CreateContestDTO dto) {
        if (dto.getProblems() != null && !dto.getProblems().isEmpty()) {
            try {
                contest.setDraftProblems(objectMapper.writeValueAsString(dto.getProblems()));
            } catch (Exception e) {
                log.warn("序列化草稿题目失败", e);
            }
        } else if ("existing_set".equals(dto.getProblemSource()) && dto.getProblemSetId() != null) {
            // 从已有题单导入：读取题单项目并存为 draftProblems
            try {
                List<ProblemSetItemDetailDTO> items = problemSetService.getProblemSetItems(dto.getProblemSetId());
                List<CreateContestDTO.ContestProblemItem> draftItems = items.stream().map(item -> {
                    CreateContestDTO.ContestProblemItem cp = new CreateContestDTO.ContestProblemItem();
                    cp.setSlug(item.getSlug());
                    cp.setScore(item.getScore());
                    return cp;
                }).collect(Collectors.toList());
                contest.setDraftProblems(objectMapper.writeValueAsString(draftItems));
            } catch (Exception e) {
                log.warn("从题单导入草稿题目失败", e);
            }
        }
    }

    /**
     * 解析 draftProblems JSON 为题目列表
     */
    private List<CreateContestDTO.ContestProblemItem> parseDraftProblems(String draftProblems) {
        if (draftProblems == null || draftProblems.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(draftProblems,
                    new TypeReference<List<CreateContestDTO.ContestProblemItem>>() {});
        } catch (Exception e) {
            log.warn("解析草稿题目失败", e);
            return List.of();
        }
    }
}
