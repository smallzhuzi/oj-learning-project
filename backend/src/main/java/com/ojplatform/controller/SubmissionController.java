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
 * 提交相关接口控制器。
 */
@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

/**
 * 提交普通练习代码。
 */
    @PostMapping
    public Result<Submission> submit(@Valid @RequestBody SubmitCodeDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        Submission submission = submissionService.submitCode(dto);
        return Result.ok(submission);
    }

/**
 * 查询普通提交的判题结果。
 */
    @GetMapping("/{id}/result")
    public Result<Submission> getResult(@PathVariable Long id) {
        Submission submission = submissionService.pollResult(id);
        return Result.ok(submission);
    }

/**
 * 查询提交详情。
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
 * 查询当前用户在指定题目的提交记录。
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
 * 查询当前用户的题目状态映射。
 */
    @GetMapping("/status-map")
    public Result<Map<String, String>> statusMap(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, String> map = submissionService.getUserStatusMap(userId);
        return Result.ok(map);
    }

/**
 * 查询当前用户的提交统计。
 */
    @GetMapping("/stats")
    public Result<UserStatsDTO> stats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserStatsDTO stats = submissionService.getUserStats(userId);
        return Result.ok(stats);
    }
}
