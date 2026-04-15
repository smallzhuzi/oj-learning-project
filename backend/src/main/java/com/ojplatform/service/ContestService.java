package com.ojplatform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ojplatform.dto.*;
import com.ojplatform.entity.Contest;
import com.ojplatform.entity.ContestSubmission;
import com.ojplatform.entity.ContestTeam;

import java.util.List;

/**
 * 比赛相关业务接口。
 */
public interface ContestService extends IService<Contest> {

    /**
     * 创建比赛（草稿状态）
     */
    Contest createContest(CreateContestDTO dto);

    /**
     * 更新比赛（仅草稿状态可修改）
     */
    void updateContest(Long contestId, CreateContestDTO dto);

    /**
     * 分页查询比赛列表
     * @param filter 筛选条件：all / my_created / my_joined
     * @param keyword 标题关键词（模糊搜索）
     * @param status 状态过滤（running / registering / ended 等）
     */
    IPage<ContestDetailDTO> listContests(Long userId, String filter, String keyword, String status, int pageNum, int pageSize);

    /**
     * 获取比赛详情
     */
    ContestDetailDTO getContestDetail(Long contestId, Long userId);

    /**
     * 发布比赛（draft → registering）
     */
    void publishContest(Long contestId, Long userId);

    /**
     * 报名比赛
     * @param teamId 组队赛时传入独立队伍 ID，个人赛传 null
     * @param memberUserIds 组队赛时传入出场成员 ID 列表，个人赛传 null
     */
    void registerContest(Long contestId, Long userId, String password, Long teamId, java.util.List<Long> memberUserIds);

    /**
     * 取消报名
     */
    void cancelRegistration(Long contestId, Long userId);

    /**
     * 创建队伍（组队赛）
     */
    ContestTeam createTeam(Long contestId, CreateTeamDTO dto);

    /**
     * 退出队伍
     */
    void leaveTeam(Long contestId, Long teamId, Long userId);

    /**
     * 获取比赛的队伍列表
     */
    List<ContestTeamLobbyDTO> getTeams(Long contestId);

    /**
     * 获取当前用户所在队伍详情
     */
    ContestTeamDetailDTO getMyTeam(Long contestId, Long userId);

    List<MyTeamSummaryDTO> getMyTeams(Long userId);

    /**
     * 修改队伍名称
     */
    ContestTeamDetailDTO updateTeam(Long contestId, Long teamId, UpdateTeamDTO dto);

    /**
     * 转让队长
     */
    void transferCaptain(Long contestId, Long teamId, TransferCaptainDTO dto);

    /**
     * 移除队员
     */
    void removeTeamMember(Long contestId, Long teamId, Long operatorUserId, Long targetUserId);

    /**
     * 解散队伍
     */
    void dissolveTeam(Long contestId, Long teamId, Long userId);

    /**
     * 比赛中提交代码
     */
    ContestSubmission submitCode(ContestSubmitDTO dto);

    /**
     * 轮询比赛提交结果
     */
    ContestSubmission pollResult(Long contestId, Long submissionId);

    /**
     * 获取用户在比赛中的提交记录
     */
    List<ContestSubmission> getMySubmissions(Long contestId, Long userId);

    /**
     * 计算并返回榜单
     */
    StandingDTO getStandings(Long contestId, Long userId);

    /**
     * 解封榜单
     */
    void unfreezeStandings(Long contestId, Long userId);

    /**
     * 获取比赛题目列表（比赛开始后才能获取）
     */
    List<ProblemSetItemDetailDTO> getContestProblems(Long contestId, Long userId);

    /**
     * 更新比赛状态（由定时任务或手动触发）
     * 检查时间自动切换状态：registering → running → frozen → ended
     */
    void refreshContestStatus(Long contestId);
}
