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
 * 管理员控制器
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 分页查询用户列表
     * GET /api/admin/users?page=1&size=20&keyword=
     */
    @GetMapping("/users")
    public Result<IPage<User>> getUserList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.getUserList(page, size, keyword));
    }

    /**
     * 切换用户状态（启用/禁用）
     * PUT /api/admin/users/{id}/toggle-status
     */
    @PutMapping("/users/{id}/toggle-status")
    public Result<String> toggleUserStatus(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return Result.ok("状态切换成功");
    }

    /**
     * 重置用户密码
     * PUT /api/admin/users/{id}/reset-password
     */
    @PutMapping("/users/{id}/reset-password")
    public Result<String> resetUserPassword(@PathVariable Long id) {
        String newPassword = adminService.resetUserPassword(id);
        return Result.ok(newPassword);
    }

    /**
     * 删除用户
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.ok("删除成功");
    }

    /**
     * 添加用户
     * POST /api/admin/users
     */
    @PostMapping("/users")
    public Result<String> createUser(@Valid @RequestBody AdminCreateUserDTO dto) {
        adminService.createUser(dto);
        return Result.ok("创建成功");
    }
}
