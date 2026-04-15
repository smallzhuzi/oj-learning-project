package com.ojplatform.controller;

import com.ojplatform.common.Result;
import com.ojplatform.dto.UpdateUserProfileDTO;
import com.ojplatform.entity.UserProfile;
import com.ojplatform.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资料接口相关接口控制器。
 */
@RestController
@RequestMapping("/api/user-profile")
public class UserProfileApiController {

    @Autowired
    private UserProfileService userProfileService;

/**
 * 查询当前用户画像。
 */
    @GetMapping
    public Result<UserProfile> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserProfile profile = userProfileService.getOrCreateProfile(userId);
        return Result.ok(profile);
    }

/**
 * 更新自我评估信息。
 */
    @PutMapping
    public Result<UserProfile> updateSelfAssessment(@RequestBody UpdateUserProfileDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dto.setUserId(userId);
        UserProfile profile = userProfileService.updateSelfAssessment(dto);
        return Result.ok(profile);
    }

/**
 * 基于做题数据分析用户画像。
 */
    @PostMapping("/analyze")
    public Result<UserProfile> analyze(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UserProfile profile = userProfileService.analyzeProfile(userId);
        return Result.ok(profile);
    }
}
