package com.ojplatform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ojplatform.common.Result;
import com.ojplatform.dto.AdminCreateUserDTO;
import com.ojplatform.entity.User;
import com.ojplatform.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理相关接口控制器。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

/**
 * 分页查询后台用户列表。
 */
    @GetMapping("/users")
    public Result<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.getUserList(page, size, keyword));
    }

/**
 * 切换指定用户的启用状态。
 */
    @PutMapping("/users/{id}/toggle-status")
    public Result<String> toggleUserStatus(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return Result.ok("状态切换成功");
    }

/**
 * 重置指定用户的登录密码。
 */
    @PutMapping("/users/{id}/reset-password")
    public Result<String> resetUserPassword(@PathVariable Long id) {
        String newPassword = adminService.resetUserPassword(id);
        return Result.ok(newPassword);
    }

/**
 * 删除指定用户。
 */
    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.ok("删除成功");
    }

/**
 * 创建后台管理用户。
 */
    @PostMapping("/users")
    public Result<String> createUser(@Valid @RequestBody AdminCreateUserDTO dto) {
        adminService.createUser(dto);
        return Result.ok("创建成功");
    }
}
