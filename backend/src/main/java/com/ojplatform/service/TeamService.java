package com.ojplatform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ojplatform.dto.*;
import com.ojplatform.entity.Team;
import com.ojplatform.entity.TeamJoinRequest;

import java.util.List;

/**
 * 队伍相关业务接口。
 */
public interface TeamService extends IService<Team> {

    /** 创建队伍 */
    Team createTeam(CreateTeamRequestDTO dto);

    /** 队伍广场分页列表 */
    IPage<TeamListDTO> listTeams(String keyword, int pageNum, int pageSize);

    /** 我的队伍列表 */
    List<TeamListDTO> getMyTeams(Long userId);

    /** 队伍详情 */
    TeamDetailDTO getTeamDetail(Long teamId, Long currentUserId);

    /** 更新队伍信息（队长） */
    void updateTeam(Long teamId, UpdateTeamRequestDTO dto);

    /** 解散队伍（队长） */
    void dissolveTeam(Long teamId, Long userId);

    /** 申请加入 */
    void applyToJoin(Long teamId, Long userId, String message);

    /** 批准申请 */
    void approveRequest(Long teamId, Long requestId, Long operatorUserId);

    /** 拒绝申请 */
    void rejectRequest(Long teamId, Long requestId, Long operatorUserId);

    /** 待审批列表 */
    List<TeamJoinRequest> getPendingRequests(Long teamId, Long operatorUserId);

    /** 退出队伍 */
    void leaveTeam(Long teamId, Long userId);

    /** 移除成员（队长） */
    void removeMember(Long teamId, Long operatorUserId, Long targetUserId);

    /** 转让队长 */
    void transferCaptain(Long teamId, Long operatorUserId, Long targetUserId);

    /** 获取用户作为队长的队伍列表（比赛报名用） */
    List<TeamListDTO> getMyCaptainTeams(Long userId);
}
