package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ojplatform.dto.AdminCreateUserDTO;
import com.ojplatform.entity.User;
import com.ojplatform.mapper.UserMapper;
import com.ojplatform.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Set;

/**
 * 管理相关业务实现。
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Set<String> ALLOWED_ROLES = Set.of("user", "admin");

    @Autowired
    private UserMapper userMapper;

/**
 * 分页查询后台用户。
 */
    @Override
    public IPage<User> getUserList(int page, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }
        wrapper.orderByDesc(User::getCreatedAt);

        IPage<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        // 脱敏：移除密码
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

/**
 * 切换用户启用状态。
 */
    @Override
    public void toggleUserStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus("active".equals(user.getStatus()) ? "disabled" : "active");
        userMapper.updateById(user);
    }

/**
 * 重置用户密码并返回新密码。
 */
    @Override
    public String resetUserPassword(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 生成随机 8 位密码
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        String newPassword = sb.toString();

        user.setPassword(PASSWORD_ENCODER.encode(newPassword));
        userMapper.updateById(user);
        return newPassword;
    }

/**
 * 删除后台用户。
 */
    @Override
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            throw new RuntimeException("不能删除管理员账号");
        }
        userMapper.deleteById(userId);
    }

/**
 * 创建后台账号。
 */
    @Override
    public void createUser(AdminCreateUserDTO dto) {
        // 检查用户名唯一
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }
        // 检查邮箱唯一
        Long emailCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, dto.getEmail()));
        if (emailCount > 0) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        String role = dto.getRole() != null ? dto.getRole().trim() : "user";
        if (!ALLOWED_ROLES.contains(role)) {
            throw new RuntimeException("角色不合法");
        }

        user.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
        user.setRole(role);
        userMapper.insert(user);
    }
}
