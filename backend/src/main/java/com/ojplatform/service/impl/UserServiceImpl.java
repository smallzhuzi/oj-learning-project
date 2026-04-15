package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ojplatform.dto.*;
import com.ojplatform.entity.User;
import com.ojplatform.mapper.UserMapper;
import com.ojplatform.service.EmailService;
import com.ojplatform.service.UserService;
import com.ojplatform.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户相关业务实现。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final BCryptPasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

/**
 * 注册用户并返回登录态。
 */
    @Override
    public LoginResponse register(RegisterDTO dto) {
        // 校验邮箱验证码
        if (!emailService.verifyCode(dto.getEmail(), dto.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 检查用户名唯一
        long usernameCount = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (usernameCount > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱唯一
        long emailCount = count(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, dto.getEmail()));
        if (emailCount > 0) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(PASSWORD_ENCODER.encode(dto.getPassword()));
        save(user);

        // 生成 Token 并返回
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

/**
 * 校验账号密码并返回登录态。
 */
    @Override
    public LoginResponse login(LoginDTO dto) {
        // 根据用户名查询
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 校验账号是否被禁用
        if ("disabled".equals(user.getStatus())) {
            throw new RuntimeException("该账号已被禁用，请联系管理员");
        }

        // 校验密码
        if (!PASSWORD_ENCODER.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 生成 Token 并返回
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

/**
 * 读取当前登录用户。
 */
    @Override
    public User getCurrentUser(Long userId) {
        User user = getById(userId);
        if (user != null) {
            user.setPassword(null); // 脱敏
        }
        return user;
    }

/**
 * 按邮箱重置用户密码。
 */
    @Override
    public void resetPassword(String email, String newPassword) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        if (user == null) {
            throw new RuntimeException("该邮箱未注册");
        }
        user.setPassword(PASSWORD_ENCODER.encode(newPassword));
        updateById(user);
    }

/**
 * 更新用户基础资料。
 */
    @Override
    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            // 修改邮箱需要验证码
            if (dto.getCode() == null || dto.getCode().isBlank()) {
                throw new RuntimeException("修改邮箱需要验证码");
            }
            if (!emailService.verifyCode(dto.getEmail(), dto.getCode())) {
                throw new RuntimeException("验证码错误或已过期");
            }
            long emailCount = count(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, dto.getEmail())
                    .ne(User::getId, userId));
            if (emailCount > 0) {
                throw new RuntimeException("该邮箱已被使用");
            }
            user.setEmail(dto.getEmail());
        }
        updateById(user);
    }

/**
 * 校验旧密码后修改新密码。
 */
    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!PASSWORD_ENCODER.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }
        user.setPassword(PASSWORD_ENCODER.encode(dto.getNewPassword()));
        updateById(user);
    }
}
