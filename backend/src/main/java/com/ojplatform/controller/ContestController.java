package com.ojplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ojplatform.common.Result;
import com.ojplatform.dto.*;
import com.ojplatform.entity.Contest;
import com.ojplatform.entity.ContestSubmission;
import com.ojplatform.entity.ContestTeam;
import com.ojplatform.service.ContestService;
import com.ojplatform.service.RedisLockService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

/**
 * 比赛相关接口控制器。
 */
@RestController
@RequestMapping("/api/contests")
public class ContestController {

    @Autowired
    private ContestService contestService;

    @Autowired
    private RedisLockService redisLockService;

/**
 * 创建比赛草稿。
 */
    @PostMapping
    public Result<Contest> create(@Valid @RequestBody CreateContestDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        Contest contest = contestService.createContest(dto);
        return Result.ok(contest);
    }

/**
 * 分页查询比赛列表。
 */
    @GetMapping
    public Result<IPage<ContestDetailDTO>> list(
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        IPage<ContestDetailDTO> page = contestService.listContests(userId, filter, keyword, status, pageNum, pageSize);
        return Result.ok(page);
    }

/**
 * 查询比赛详情。
 */
    @GetMapping("/{id}")
    public Result<ContestDetailDTO> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ContestDetailDTO detail = contestService.getContestDetail(id, userId);
        return Result.ok(detail);
    }

/**
 * 更新比赛配置。
 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CreateContestDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        contestService.updateContest(id, dto);
        return Result.ok();
    }

/**
 * 发布比赛。
 */
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contestService.publishContest(id, userId);
        return Result.ok();
    }

/**
 * 为个人赛或队伍赛执行报名。
 */
    @PostMapping("/{id}/register")
    public Result<Void> register(@PathVariable Long id,
                                  @RequestParam(required = false) String password,
                                  @RequestParam(required = false) Long teamId,
                                  @RequestBody(required = false) java.util.List<Long> memberUserIds,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        redisLockService.executeWithLock("contest:register:" + id, Duration.ofSeconds(5),
                () -> contestService.registerContest(id, userId, password, teamId, memberUserIds));
        return Result.ok();
    }

/**
 * 取消比赛报名。
 */
    @DeleteMapping("/{id}/register")
    public Result<Void> cancelRegister(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contestService.cancelRegistration(id, userId);
        return Result.ok();
    }

/**
 * 在比赛中创建队伍。
 */
    @PostMapping("/{id}/teams")
    public Result<ContestTeam> createTeam(@PathVariable Long id, @Valid @RequestBody CreateTeamDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        ContestTeam team = contestService.createTeam(id, dto);
        return Result.ok(team);
    }

/**
 * 退出当前比赛队伍。
 */
    @DeleteMapping("/{id}/teams/{teamId}/leave")
    public Result<Void> leaveTeam(@PathVariable Long id, @PathVariable Long teamId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contestService.leaveTeam(id, teamId, userId);
        return Result.ok();
    }

/**
 * 查询比赛队伍广场列表。
 */
    @GetMapping("/{id}/teams")
    public Result<List<ContestTeamLobbyDTO>> teams(@PathVariable Long id) {
        List<ContestTeamLobbyDTO> teams = contestService.getTeams(id);
        return Result.ok(teams);
    }

/**
 * 查询当前用户在比赛中的队伍。
 */
    @GetMapping("/{id}/teams/my")
    public Result<ContestTeamDetailDTO> myTeam(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(contestService.getMyTeam(id, userId));
    }

/**
 * 查询当前用户参与的比赛队伍。
 */
    @GetMapping("/teams/my")
    public Result<List<MyTeamSummaryDTO>> myTeams(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(contestService.getMyTeams(userId));
    }

/**
 * 更新比赛队伍信息。
 */
    @PutMapping("/{id}/teams/{teamId}")
    public Result<ContestTeamDetailDTO> updateTeam(@PathVariable Long id,
                                                   @PathVariable Long teamId,
                                                   @Valid @RequestBody UpdateTeamDTO dto,
                                                   HttpServletRequest request) {
        dto.setUserId((Long) request.getAttribute("userId"));
        return Result.ok(contestService.updateTeam(id, teamId, dto));
    }

/**
 * 转让比赛队伍队长。
 */
    @PostMapping("/{id}/teams/{teamId}/transfer-captain")
    public Result<Void> transferCaptain(@PathVariable Long id,
                                        @PathVariable Long teamId,
                                        @Valid @RequestBody TransferCaptainDTO dto,
                                        HttpServletRequest request) {
        dto.setUserId((Long) request.getAttribute("userId"));
        contestService.transferCaptain(id, teamId, dto);
        return Result.ok();
    }

/**
 * 移除比赛队伍成员。
 */
    @DeleteMapping("/{id}/teams/{teamId}/members/{targetUserId}")
    public Result<Void> removeMember(@PathVariable Long id,
                                     @PathVariable Long teamId,
                                     @PathVariable Long targetUserId,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contestService.removeTeamMember(id, teamId, userId, targetUserId);
        return Result.ok();
    }

/**
 * 解散比赛队伍。
 */
    @DeleteMapping("/{id}/teams/{teamId}")
    public Result<Void> dissolveTeam(@PathVariable Long id,
                                     @PathVariable Long teamId,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contestService.dissolveTeam(id, teamId, userId);
        return Result.ok();
    }

/**
 * 提交比赛代码。
 */
    @PostMapping("/{id}/submit")
    public Result<ContestSubmission> submit(@PathVariable Long id, @Valid @RequestBody ContestSubmitDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        dto.setContestId(id);
        ContestSubmission cs = contestService.submitCode(dto);
        return Result.ok(cs);
    }

/**
 * 查询比赛提交结果。
 */
    @GetMapping("/{id}/submissions/{subId}/result")
    public Result<ContestSubmission> pollResult(@PathVariable Long id, @PathVariable Long subId) {
        ContestSubmission cs = contestService.pollResult(id, subId);
        return Result.ok(cs);
    }

/**
 * 查询当前用户的比赛提交记录。
 */
    @GetMapping("/{id}/submissions")
    public Result<List<ContestSubmission>> mySubmissions(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ContestSubmission> subs = contestService.getMySubmissions(id, userId);
        return Result.ok(subs);
    }

/**
 * 查询比赛榜单。
 */
    @GetMapping("/{id}/standings")
    public Result<StandingDTO> standings(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StandingDTO standing = contestService.getStandings(id, userId);
        return Result.ok(standing);
    }

/**
 * 执行比赛解榜。
 */
    @PostMapping("/{id}/unfreeze")
    public Result<Void> unfreeze(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contestService.unfreezeStandings(id, userId);
        return Result.ok();
    }

/**
 * 查询比赛题目列表。
 */
    @GetMapping("/{id}/problems")
    public Result<List<ProblemSetItemDetailDTO>> contestProblems(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ProblemSetItemDetailDTO> items = contestService.getContestProblems(id, userId);
        return Result.ok(items);
    }
}
