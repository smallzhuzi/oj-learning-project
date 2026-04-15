package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ojplatform.dto.*;
import com.ojplatform.entity.*;
import com.ojplatform.mapper.*;
import com.ojplatform.service.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 队伍相关业务实现。
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamServiceImpl.class);

    @Autowired
    private TeamMemberMapper teamMemberMapper;
    @Autowired
    private TeamJoinRequestMapper joinRequestMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ContestRegistrationMapper registrationMapper;
    @Autowired
    private ContestMapper contestMapper;

/**
 * 创建独立队伍并写入队长成员关系。
 */
    @Override
    @Transactional
    public Team createTeam(CreateTeamRequestDTO dto) {
        Team team = new Team();
        team.setTeamName(dto.getTeamName().trim());
        team.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        team.setCaptainId(dto.getUserId());
        team.setMemberCount(1);
        baseMapper.insert(team);

        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(dto.getUserId());
        member.setRole("captain");
        teamMemberMapper.insert(member);

        log.info("创建队伍：id={}, name={}, captain={}", team.getId(), team.getTeamName(), dto.getUserId());
        return team;
    }

/**
 * 分页查询队伍广场。
 */
    @Override
    public IPage<TeamListDTO> listTeams(String keyword, int pageNum, int pageSize) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<Team>()
                .orderByDesc(Team::getCreatedAt);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Team::getTeamName, keyword.trim());
        }

        IPage<Team> teamPage = baseMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<TeamListDTO> result = new Page<>(pageNum, pageSize);
        result.setTotal(teamPage.getTotal());
        result.setRecords(teamPage.getRecords().stream().map(this::toTeamListDTO).collect(Collectors.toList()));
        return result;
    }

/**
 * 查询当前用户加入的队伍。
 */
    @Override
    public List<TeamListDTO> getMyTeams(Long userId) {
        List<TeamMember> memberships = teamMemberMapper.selectList(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getUserId, userId)
                        .orderByDesc(TeamMember::getJoinedAt)
        );
        if (memberships.isEmpty()) return List.of();

        List<Long> teamIds = memberships.stream().map(TeamMember::getTeamId).collect(Collectors.toList());
        List<Team> teams = baseMapper.selectBatchIds(teamIds);

        Map<Long, Team> teamMap = teams.stream().collect(Collectors.toMap(Team::getId, t -> t));
        return teamIds.stream()
                .map(teamMap::get)
                .filter(Objects::nonNull)
                .map(this::toTeamListDTO)
                .collect(Collectors.toList());
    }

/**
 * 组装队伍详情和成员信息。
 */
    @Override
    public TeamDetailDTO getTeamDetail(Long teamId, Long currentUserId) {
        Team team = baseMapper.selectById(teamId);
        if (team == null) throw new RuntimeException("队伍不存在");

        TeamDetailDTO dto = new TeamDetailDTO();
        dto.setId(team.getId());
        dto.setTeamName(team.getTeamName());
        dto.setDescription(team.getDescription());
        dto.setCaptainId(team.getCaptainId());
        dto.setMemberCount(team.getMemberCount());
        dto.setCreatedAt(team.getCreatedAt());
        dto.setCaptain(team.getCaptainId().equals(currentUserId));

        User captain = userMapper.selectById(team.getCaptainId());
        dto.setCaptainName(captain != null ? captain.getUsername() : "未知");

        // 成员列表
        List<TeamMember> members = teamMemberMapper.selectList(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId)
                        .orderByAsc(TeamMember::getJoinedAt)
        );
        List<TeamDetailDTO.MemberInfo> memberInfos = new ArrayList<>();
        for (TeamMember m : members) {
            TeamDetailDTO.MemberInfo info = new TeamDetailDTO.MemberInfo();
            info.setUserId(m.getUserId());
            info.setRole(m.getRole());
            info.setJoinedAt(m.getJoinedAt());
            User u = userMapper.selectById(m.getUserId());
            info.setUsername(u != null ? u.getUsername() : "未知用户");
            if (m.getUserId().equals(currentUserId)) {
                dto.setMyRole(m.getRole());
            }
            memberInfos.add(info);
        }
        dto.setMembers(memberInfos);

        // 参赛记录：通过 contest_registrations 查找
        List<ContestRegistration> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getTeamId, teamId)
                        .orderByDesc(ContestRegistration::getRegisteredAt)
        );
        List<TeamDetailDTO.ContestRecord> records = new ArrayList<>();
        for (ContestRegistration reg : registrations) {
            Contest contest = contestMapper.selectById(reg.getContestId());
            if (contest == null) continue;
            TeamDetailDTO.ContestRecord cr = new TeamDetailDTO.ContestRecord();
            cr.setContestId(contest.getId());
            cr.setContestTitle(contest.getTitle());
            cr.setContestStatus(contest.getStatus());
            cr.setRegistrationStatus(reg.getStatus());
            cr.setStartTime(contest.getStartTime());
            records.add(cr);
        }
        dto.setContests(records);

        // 待审批数
        if (team.getCaptainId().equals(currentUserId)) {
            Long pendingCount = joinRequestMapper.selectCount(
                    new LambdaQueryWrapper<TeamJoinRequest>()
                            .eq(TeamJoinRequest::getTeamId, teamId)
                            .eq(TeamJoinRequest::getStatus, "pending")
            );
            dto.setPendingRequestCount(pendingCount.intValue());
        }

        return dto;
    }

/**
 * 更新队伍基本资料。
 */
    @Override
    @Transactional
    public void updateTeam(Long teamId, UpdateTeamRequestDTO dto) {
        Team team = requireCaptain(teamId, dto.getUserId());
        if (dto.getTeamName() != null && !dto.getTeamName().isBlank()) {
            team.setTeamName(dto.getTeamName().trim());
        }
        if (dto.getDescription() != null) {
            team.setDescription(dto.getDescription().trim().isEmpty() ? null : dto.getDescription().trim());
        }
        baseMapper.updateById(team);
    }

/**
 * 解散队伍并清理关联成员。
 */
    @Override
    @Transactional
    public void dissolveTeam(Long teamId, Long userId) {
        requireCaptain(teamId, userId);

        // 取消所有关联的比赛报名
        List<ContestRegistration> regs = registrationMapper.selectList(
                new LambdaQueryWrapper<ContestRegistration>()
                        .eq(ContestRegistration::getTeamId, teamId)
                        .eq(ContestRegistration::getStatus, "registered")
        );
        for (ContestRegistration reg : regs) {
            reg.setStatus("cancelled");
            registrationMapper.updateById(reg);
        }

        teamMemberMapper.delete(new LambdaQueryWrapper<TeamMember>().eq(TeamMember::getTeamId, teamId));
        joinRequestMapper.delete(new LambdaQueryWrapper<TeamJoinRequest>().eq(TeamJoinRequest::getTeamId, teamId));
        baseMapper.deleteById(teamId);
        log.info("解散队伍：id={}", teamId);
    }

/**
 * 提交入队申请。
 */
    @Override
    @Transactional
    public void applyToJoin(Long teamId, Long userId, String message) {
        Team team = baseMapper.selectById(teamId);
        if (team == null) throw new RuntimeException("队伍不存在");

        checkNotInTeam(teamId, userId);

        // 检查是否已有 pending 申请
        Long exists = joinRequestMapper.selectCount(
                new LambdaQueryWrapper<TeamJoinRequest>()
                        .eq(TeamJoinRequest::getTeamId, teamId)
                        .eq(TeamJoinRequest::getUserId, userId)
                        .eq(TeamJoinRequest::getStatus, "pending")
        );
        if (exists > 0) throw new RuntimeException("你已提交过申请，请等待审批");

        TeamJoinRequest req = new TeamJoinRequest();
        req.setTeamId(teamId);
        req.setUserId(userId);
        req.setMessage(message);
        req.setStatus("pending");
        joinRequestMapper.insert(req);
    }

/**
 * 通过入队申请并写入成员关系。
 */
    @Override
    @Transactional
    public void approveRequest(Long teamId, Long requestId, Long operatorUserId) {
        requireCaptain(teamId, operatorUserId);
        TeamJoinRequest req = joinRequestMapper.selectById(requestId);
        if (req == null || !req.getTeamId().equals(teamId)) throw new RuntimeException("申请不存在");
        if (!"pending".equals(req.getStatus())) throw new RuntimeException("该申请已处理");

        req.setStatus("approved");
        joinRequestMapper.updateById(req);

        Team team = baseMapper.selectById(teamId);
        addMember(team, req.getUserId());
    }

/**
 * 拒绝入队申请。
 */
    @Override
    @Transactional
    public void rejectRequest(Long teamId, Long requestId, Long operatorUserId) {
        requireCaptain(teamId, operatorUserId);
        TeamJoinRequest req = joinRequestMapper.selectById(requestId);
        if (req == null || !req.getTeamId().equals(teamId)) throw new RuntimeException("申请不存在");
        if (!"pending".equals(req.getStatus())) throw new RuntimeException("该申请已处理");

        req.setStatus("rejected");
        joinRequestMapper.updateById(req);
    }

/**
 * 查询待处理的入队申请。
 */
    @Override
    public List<TeamJoinRequest> getPendingRequests(Long teamId, Long operatorUserId) {
        requireCaptain(teamId, operatorUserId);
        return joinRequestMapper.selectList(
                new LambdaQueryWrapper<TeamJoinRequest>()
                        .eq(TeamJoinRequest::getTeamId, teamId)
                        .eq(TeamJoinRequest::getStatus, "pending")
                        .orderByDesc(TeamJoinRequest::getCreatedAt)
        );
    }

/**
 * 让普通成员退出队伍。
 */
    @Override
    @Transactional
    public void leaveTeam(Long teamId, Long userId) {
        Team team = baseMapper.selectById(teamId);
        if (team == null) throw new RuntimeException("队伍不存在");
        if (team.getCaptainId().equals(userId)) throw new RuntimeException("队长不能直接退出，请先转让队长或解散队伍");

        teamMemberMapper.delete(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId)
                        .eq(TeamMember::getUserId, userId)
        );
        team.setMemberCount(Math.max(0, team.getMemberCount() - 1));
        baseMapper.updateById(team);
    }

/**
 * 由队长移除队伍成员。
 */
    @Override
    @Transactional
    public void removeMember(Long teamId, Long operatorUserId, Long targetUserId) {
        requireCaptain(teamId, operatorUserId);
        if (operatorUserId.equals(targetUserId)) throw new RuntimeException("不能移除自己");

        TeamMember target = teamMemberMapper.selectOne(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId)
                        .eq(TeamMember::getUserId, targetUserId)
        );
        if (target == null) throw new RuntimeException("目标成员不在队伍中");

        teamMemberMapper.deleteById(target.getId());
        Team team = baseMapper.selectById(teamId);
        team.setMemberCount(Math.max(0, team.getMemberCount() - 1));
        baseMapper.updateById(team);
    }

/**
 * 转让队长身份。
 */
    @Override
    @Transactional
    public void transferCaptain(Long teamId, Long operatorUserId, Long targetUserId) {
        Team team = requireCaptain(teamId, operatorUserId);
        if (operatorUserId.equals(targetUserId)) throw new RuntimeException("无需转让给自己");

        TeamMember targetMember = teamMemberMapper.selectOne(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId)
                        .eq(TeamMember::getUserId, targetUserId)
        );
        if (targetMember == null) throw new RuntimeException("目标成员不在队伍中");

        TeamMember captainMember = teamMemberMapper.selectOne(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId)
                        .eq(TeamMember::getUserId, operatorUserId)
        );
        captainMember.setRole("member");
        targetMember.setRole("captain");
        teamMemberMapper.updateById(captainMember);
        teamMemberMapper.updateById(targetMember);

        team.setCaptainId(targetUserId);
        baseMapper.updateById(team);
    }

/**
 * 查询当前用户担任队长的队伍。
 */
    @Override
    public List<TeamListDTO> getMyCaptainTeams(Long userId) {
        List<Team> teams = baseMapper.selectList(
                new LambdaQueryWrapper<Team>()
                        .eq(Team::getCaptainId, userId)
                        .orderByDesc(Team::getCreatedAt)
        );
        return teams.stream().map(this::toTeamListDTO).collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    private TeamListDTO toTeamListDTO(Team team) {
        TeamListDTO dto = new TeamListDTO();
        dto.setId(team.getId());
        dto.setTeamName(team.getTeamName());
        dto.setDescription(team.getDescription());
        dto.setCaptainId(team.getCaptainId());
        dto.setMemberCount(team.getMemberCount());
        dto.setCreatedAt(team.getCreatedAt());

        User captain = userMapper.selectById(team.getCaptainId());
        dto.setCaptainName(captain != null ? captain.getUsername() : "未知");

        List<TeamMember> members = teamMemberMapper.selectList(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, team.getId())
                        .orderByAsc(TeamMember::getJoinedAt)
        );
        dto.setMemberNames(members.stream().map(m -> {
            User u = userMapper.selectById(m.getUserId());
            return u != null ? u.getUsername() : "未知";
        }).collect(Collectors.toList()));

        return dto;
    }

    private Team requireCaptain(Long teamId, Long userId) {
        Team team = baseMapper.selectById(teamId);
        if (team == null) throw new RuntimeException("队伍不存在");
        if (!team.getCaptainId().equals(userId)) throw new RuntimeException("仅队长可执行该操作");
        return team;
    }

    private void checkNotInTeam(Long teamId, Long userId) {
        Long exists = teamMemberMapper.selectCount(
                new LambdaQueryWrapper<TeamMember>()
                        .eq(TeamMember::getTeamId, teamId)
                        .eq(TeamMember::getUserId, userId)
        );
        if (exists > 0) throw new RuntimeException("你已在该队伍中");
    }

    private void addMember(Team team, Long userId) {
        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(userId);
        member.setRole("member");
        teamMemberMapper.insert(member);

        team.setMemberCount(team.getMemberCount() + 1);
        baseMapper.updateById(team);
    }
}
