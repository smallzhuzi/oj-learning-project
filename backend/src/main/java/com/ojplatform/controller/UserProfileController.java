package com.ojplatform.controller;

import com.ojplatform.common.Result;
import com.ojplatform.dto.ChangePasswordDTO;
import com.ojplatform.dto.UpdateProfileDTO;
import com.ojplatform.dto.UserOjConfigDTO;
import com.ojplatform.entity.User;
import com.ojplatform.entity.UserOjConfig;
import com.ojplatform.service.UserOjConfigService;
import com.ojplatform.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户个人中心控制器
 */
@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserOjConfigService userOjConfigService;

    /**
     * 修改个人资料
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(HttpServletRequest request,
                                        @Valid @RequestBody UpdateProfileDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateProfile(userId, dto);
        return Result.ok("修改成功");
    }

    /**
     * 修改密码
     * PUT /api/user/password
     */
    @PutMapping("/password")
    public Result<String> changePassword(HttpServletRequest request,
                                         @Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, dto);
        return Result.ok("密码修改成功");
    }

    /**
     * 获取当前用户信息
     * GET /api/user/info
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getCurrentUser(userId);
        return Result.ok(user);
    }

    /**
     * 获取用户所有 OJ 配置
     * GET /api/user/oj-configs
     */
    @GetMapping("/oj-configs")
    public Result<List<UserOjConfig>> getOjConfigs(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userOjConfigService.getUserConfigs(userId));
    }

    /**
     * 保存或更新 OJ 配置
     * PUT /api/user/oj-config
     */
    @PutMapping("/oj-config")
    public Result<String> saveOjConfig(HttpServletRequest request,
                                       @Valid @RequestBody UserOjConfigDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        userOjConfigService.saveOrUpdateConfig(userId, dto);
        return Result.ok("配置保存成功");
    }
}
