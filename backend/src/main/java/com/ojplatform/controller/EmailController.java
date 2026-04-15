package com.ojplatform.controller;

import com.ojplatform.common.Result;
import com.ojplatform.dto.ResetPasswordDTO;
import com.ojplatform.dto.SendCodeDTO;
import com.ojplatform.service.EmailService;
import com.ojplatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件相关接口控制器。
 */
@RestController
@RequestMapping("/api/auth")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

/**
 * 发送邮箱验证码。
 */
    @PostMapping("/send-code")
    public Result<String> sendCode(@Valid @RequestBody SendCodeDTO dto) {
        emailService.sendVerificationCode(dto.getEmail());
        return Result.ok("验证码已发送");
    }

/**
 * 校验验证码并重置密码。
 */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        // 先校验验证码
        if (!emailService.verifyCode(dto.getEmail(), dto.getCode())) {
            return Result.error(400, "验证码错误或已过期");
        }
        // 重置密码
        userService.resetPassword(dto.getEmail(), dto.getNewPassword());
        return Result.ok("密码重置成功");
    }
}
