package com.ojplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ojplatform.common.Result;
import com.ojplatform.dto.*;
import com.ojplatform.entity.Team;
import com.ojplatform.entity.TeamJoinRequest;
import com.ojplatform.service.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 队伍相关接口控制器。
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

/**
 * 创建独立队伍。
 */
    @PostMapping
    public Result<Team> create(@Valid @RequestBody CreateTeamRequestDTO dto, HttpServletRequest request) {
        dto.setUserId((Long) request.getAttribute("userId"));
        return Result.ok(teamService.createTeam(dto));
    }

/**
 * 分页查询队伍列表。
 */
    @GetMapping
    public Result<IPage<TeamListDTO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(teamService.listTeams(keyword, pageNum, pageSize));
    }

/**
 * 查询我加入的队伍。
 */
    @GetMapping("/my")
    public Result<List<TeamListDTO>> myTeams(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(teamService.getMyTeams(userId));
    }

/**
 * 查询我管理的队伍。
 */
    @GetMapping("/my-captain")
    public Result<List<TeamListDTO>> myCaptainTeams(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(teamService.getMyCaptainTeams(userId));
    }

/**
 * 查询队伍详情。
 */
    @GetMapping("/{id}")
    public Result<TeamDetailDTO> detail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(teamService.getTeamDetail(id, userId));
    }

/**
 * 更新队伍信息。
 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UpdateTeamRequestDTO dto, HttpServletRequest request) {
        dto.setUserId((Long) request.getAttribute("userId"));
        teamService.updateTeam(id, dto);
        return Result.ok();
    }

/**
 * 解散队伍。
 */
    @DeleteMapping("/{id}")
    public Result<Void> dissolve(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        teamService.dissolveTeam(id, userId);
        return Result.ok();
    }

/**
 * 申请加入队伍。
 */
    @PostMapping("/{id}/apply")
    public Result<Void> apply(@PathVariable Long id, @RequestParam(required = false) String message, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        teamService.applyToJoin(id, userId, message);
        return Result.ok();
    }

/**
 * 通过入队申请。
 */
    @PostMapping("/{id}/requests/{reqId}/approve")
    public Result<Void> approve(@PathVariable Long id, @PathVariable Long reqId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        teamService.approveRequest(id, reqId, userId);
        return Result.ok();
    }

/**
 * 拒绝入队申请。
 */
    @PostMapping("/{id}/requests/{reqId}/reject")
    public Result<Void> reject(@PathVariable Long id, @PathVariable Long reqId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        teamService.rejectRequest(id, reqId, userId);
        return Result.ok();
    }

/**
 * 查询待处理的入队申请。
 */
    @GetMapping("/{id}/requests")
    public Result<List<TeamJoinRequest>> requests(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(teamService.getPendingRequests(id, userId));
    }

/**
 * 退出队伍。
 */
    @PostMapping("/{id}/leave")
    public Result<Void> leave(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        teamService.leaveTeam(id, userId);
        return Result.ok();
    }

/**
 * 移除队伍成员。
 */
    @DeleteMapping("/{id}/members/{targetUserId}")
    public Result<Void> removeMember(@PathVariable Long id, @PathVariable Long targetUserId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        teamService.removeMember(id, userId, targetUserId);
        return Result.ok();
    }

/**
 * 转让队长。
 */
    @PostMapping("/{id}/transfer")
    public Result<Void> transfer(@PathVariable Long id, @RequestParam Long targetUserId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        teamService.transferCaptain(id, userId, targetUserId);
        return Result.ok();
    }

}
