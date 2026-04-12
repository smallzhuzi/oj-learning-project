package com.ojplatform.controller;

import com.ojplatform.common.Result;
import com.ojplatform.dto.UpdateUserProfileDTO;
import com.ojplatform.entity.UserProfile;
import com.ojplatform.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户画像控制器
 * 提供用户画像查看、自评更新、自动分析等接口
 */
@RestController
@RequestMapping("/api/user-profile")
public class UserProfileApiController {

    @Autowired
    private UserProfileService userProfileService;

    /**
     * 获取当前用户画像
     * GET /api/user-profile
     */
    @GetMapping
    public Result<UserProfile> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserProfile profile = userProfileService.getOrCreateProfile(userId);
        return Result.ok(profile);
    }

    /**
     * 更新用户自评水平和目标
     * PUT /api/user-profile
     */
    @PutMapping
    public Result<UserProfile> updateSelfAssessment(@RequestBody UpdateUserProfileDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        UserProfile profile = userProfileService.updateSelfAssessment(dto);
        return Result.ok(profile);
    }

    /**
     * 触发用户画像自动分析
     * POST /api/user-profile/analyze
     */
    @PostMapping("/analyze")
    public Result<UserProfile> analyze(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserProfile profile = userProfileService.analyzeProfile(userId);
        return Result.ok(profile);
    }
}
