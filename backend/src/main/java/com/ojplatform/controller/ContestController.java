package com.ojplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ojplatform.common.Result;
import com.ojplatform.dto.*;
import com.ojplatform.entity.Contest;
import com.ojplatform.entity.ContestSubmission;
import com.ojplatform.entity.ContestTeam;
import com.ojplatform.service.ContestService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 比赛控制器
 * 提供比赛 CRUD、报名、组队、提交、榜单等接口
 */
@RestController
@RequestMapping("/api/contests")
public class ContestController {

    @Autowired
    private ContestService contestService;

    /**
     * 创建比赛
     * POST /api/contests
     */
    @PostMapping
    public Result<Contest> create(@Valid @RequestBody CreateContestDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        Contest contest = contestService.createContest(dto);
        return Result.ok(contest);
    }

    /**
     * 比赛列表
     * GET /api/contests?filter=all&keyword=xxx&status=running&pageNum=1&pageSize=20
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
     * 比赛详情
     * GET /api/contests/{id}
     */
    @GetMapping("/{id}")
    public Result<ContestDetailDTO> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        ContestDetailDTO detail = contestService.getContestDetail(id, userId);
        return Result.ok(detail);
    }

    /**
     * 更新比赛（仅草稿状态可修改）
     * PUT /api/contests/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CreateContestDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        contestService.updateContest(id, dto);
        return Result.ok();
    }

    /**
     * 发布比赛（draft → registering）
     * POST /api/contests/{id}/publish
     */
    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contestService.publishContest(id, userId);
        return Result.ok();
    }

    /**
     * 报名比赛
     * POST /api/contests/{id}/register
     */
    @PostMapping("/{id}/register")
    public Result<Void> register(@PathVariable Long id,
                                  @RequestParam(required = false) String password,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contestService.registerContest(id, userId, password);
        return Result.ok();
    }

    /**
     * 取消报名
     * DELETE /api/contests/{id}/register
     */
    @DeleteMapping("/{id}/register")
    public Result<Void> cancelRegister(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contestService.cancelRegistration(id, userId);
        return Result.ok();
    }

    /**
     * 创建队伍（组队赛）
     * POST /api/contests/{id}/teams
     */
    @PostMapping("/{id}/teams")
    public Result<ContestTeam> createTeam(@PathVariable Long id, @Valid @RequestBody CreateTeamDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        ContestTeam team = contestService.createTeam(id, dto);
        return Result.ok(team);
    }

    /**
     * 通过邀请码加入队伍
     * POST /api/contests/{id}/teams/join
     */
    @PostMapping("/{id}/teams/join")
    public Result<Void> joinTeam(@PathVariable Long id, @Valid @RequestBody JoinTeamDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        contestService.joinTeam(id, dto);
        return Result.ok();
    }

    /**
     * 退出队伍
     * DELETE /api/contests/{id}/teams/{teamId}/leave
     */
    @DeleteMapping("/{id}/teams/{teamId}/leave")
    public Result<Void> leaveTeam(@PathVariable Long id, @PathVariable Long teamId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contestService.leaveTeam(id, teamId, userId);
        return Result.ok();
    }

    /**
     * 获取比赛的队伍列表
     * GET /api/contests/{id}/teams
     */
    @GetMapping("/{id}/teams")
    public Result<List<ContestTeam>> teams(@PathVariable Long id) {
        List<ContestTeam> teams = contestService.getTeams(id);
        return Result.ok(teams);
    }

    /**
     * 比赛中提交代码
     * POST /api/contests/{id}/submit
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
     * 轮询比赛提交结果
     * GET /api/contests/{id}/submissions/{subId}/result
     */
    @GetMapping("/{id}/submissions/{subId}/result")
    public Result<ContestSubmission> pollResult(@PathVariable Long id, @PathVariable Long subId) {
        ContestSubmission cs = contestService.pollResult(id, subId);
        return Result.ok(cs);
    }

    /**
     * 获取我在比赛中的提交记录
     * GET /api/contests/{id}/submissions
     */
    @GetMapping("/{id}/submissions")
    public Result<List<ContestSubmission>> mySubmissions(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ContestSubmission> subs = contestService.getMySubmissions(id, userId);
        return Result.ok(subs);
    }

    /**
     * 获取榜单
     * GET /api/contests/{id}/standings
     */
    @GetMapping("/{id}/standings")
    public Result<StandingDTO> standings(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        StandingDTO standing = contestService.getStandings(id, userId);
        return Result.ok(standing);
    }

    /**
     * 解封榜单
     * POST /api/contests/{id}/unfreeze
     */
    @PostMapping("/{id}/unfreeze")
    public Result<Void> unfreeze(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        contestService.unfreezeStandings(id, userId);
        return Result.ok();
    }

    /**
     * 获取比赛题目列表
     * GET /api/contests/{id}/problems
     */
    @GetMapping("/{id}/problems")
    public Result<List<ProblemSetItemDetailDTO>> contestProblems(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ProblemSetItemDetailDTO> items = contestService.getContestProblems(id, userId);
        return Result.ok(items);
    }
}
