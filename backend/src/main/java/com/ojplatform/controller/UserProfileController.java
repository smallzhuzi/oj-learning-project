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
 * 用户资料相关接口控制器。
 */
@RestController
@RequestMapping("/api/user")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserOjConfigService userOjConfigService;

/**
 * 更新当前用户基础资料。
 */
    @PutMapping("/profile")
    public Result<String> updateProfile(HttpServletRequest request,
                                        @Valid @RequestBody UpdateProfileDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        userService.updateProfile(userId, dto);
        return Result.ok("修改成功");
    }

/**
 * 修改当前用户密码。
 */
    @PutMapping("/password")
    public Result<String> changePassword(HttpServletRequest request,
                                         @Valid @RequestBody ChangePasswordDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, dto);
        return Result.ok("密码修改成功");
    }

/**
 * 查询当前用户信息。
 */
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getCurrentUser(userId);
        return Result.ok(user);
    }

/**
 * 查询当前用户的 OJ 配置。
 */
    @GetMapping("/oj-configs")
    public Result<List<UserOjConfig>> getOjConfigs(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(userOjConfigService.getUserConfigs(userId));
    }

/**
 * 保存当前用户的 OJ 配置。
 */
    @PutMapping("/oj-config")
    public Result<String> saveOjConfig(HttpServletRequest request,
                                       @Valid @RequestBody UserOjConfigDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        userOjConfigService.saveOrUpdateConfig(userId, dto);
        return Result.ok("配置保存成功");
    }
}
