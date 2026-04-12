package com.ojplatform.controller;

import com.ojplatform.common.Result;
import com.ojplatform.dto.SubmitCodeDTO;
import com.ojplatform.dto.UserStatsDTO;
import com.ojplatform.entity.Submission;
import com.ojplatform.service.SubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 代码提交控制器
 * 提供代码提交、判题结果查询等接口
 */
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    /**
     * 提交代码
     * 前端将代码发送到后端，后端转发至远程 OJ 判题
     *
     * POST /api/submissions
     */
    @PostMapping
    public Result<Submission> submit(@Valid @RequestBody SubmitCodeDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        Submission submission = submissionService.submitCode(dto);
        return Result.ok(submission);
    }

    /**
     * 查询判题结果（轮询接口）
     * 前端定时调用此接口获取最新判题状态
     *
     * GET /api/submissions/{id}/result
     */
    @GetMapping("/{id}/result")
    public Result<Submission> getResult(@PathVariable Long id) {
        Submission submission = submissionService.pollResult(id);
        return Result.ok(submission);
    }

    /**
     * 根据 ID 查询提交记录详情
     *
     * GET /api/submissions/{id}
     */
    @GetMapping("/{id}")
    public Result<Submission> detail(@PathVariable Long id) {
        Submission submission = submissionService.getById(id);
        if (submission == null) {
            return Result.error(404, "提交记录不存在");
        }
        return Result.ok(submission);
    }

    /**
     * 查询当前用户在某题上的所有提交记录（按时间倒序）
     *
     * GET /api/submissions/problem/{slug}
     */
    @GetMapping("/problem/{slug}")
    public Result<List<Submission>> problemSubmissions(
            @PathVariable String slug,
            @RequestParam(defaultValue = "leetcode") String ojPlatform,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Submission> submissions = submissionService.getUserProblemSubmissions(userId, slug, ojPlatform);
        return Result.ok(submissions);
    }

    /**
     * 批量查询当前用户所有已提交题目的状态摘要
     * 返回 slug → "accepted" / "attempted"
     *
     * GET /api/submissions/status-map
     */
    @GetMapping("/status-map")
    public Result<Map<String, String>> statusMap(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, String> map = submissionService.getUserStatusMap(userId);
        return Result.ok(map);
    }

    /**
     * 用户做题统计（按平台、难度、近期趋势）
     *
     * GET /api/submissions/stats
     */
    @GetMapping("/stats")
    public Result<UserStatsDTO> stats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserStatsDTO stats = submissionService.getUserStats(userId);
        return Result.ok(stats);
    }
}
