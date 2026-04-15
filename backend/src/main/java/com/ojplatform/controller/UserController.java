package com.ojplatform.controller;

import com.ojplatform.common.Result;
import com.ojplatform.dto.LoginDTO;
import com.ojplatform.dto.LoginResponse;
import com.ojplatform.dto.RegisterDTO;
import com.ojplatform.entity.User;
import com.ojplatform.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户相关接口控制器。
 */
@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

/**
 * 注册新用户。
 */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterDTO dto) {
        LoginResponse response = userService.register(dto);
        return Result.ok(response);
    }

/**
 * 执行用户登录。
 */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginDTO dto) {
        LoginResponse response = userService.login(dto);
        return Result.ok(response);
    }

/**
 * 查询当前登录用户信息。
 */
    @GetMapping("/me")
    public Result<User> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getCurrentUser(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.ok(user);
    }
}
