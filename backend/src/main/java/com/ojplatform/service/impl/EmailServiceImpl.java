package com.ojplatform.service.impl;

import com.ojplatform.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

/**
 * 邮件相关业务实现。
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration SEND_INTERVAL = Duration.ofSeconds(60);
    private static final String CODE_KEY_PREFIX = "auth:email:code:";
    private static final String RATE_LIMIT_KEY_PREFIX = "auth:email:send:";

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

/**
 * 生成验证码并发送到邮箱。
 */
    @Override
    public void sendVerificationCode(String email) {
        String codeKey = CODE_KEY_PREFIX + email;
        String rateLimitKey = RATE_LIMIT_KEY_PREFIX + email;

        Boolean allowed = stringRedisTemplate.opsForValue().setIfAbsent(rateLimitKey, "1", SEND_INTERVAL);
        if (!Boolean.TRUE.equals(allowed)) {
            throw new RuntimeException("发送太频繁，请 60 秒后再试");
        }

        String code = String.valueOf(100000 + SECURE_RANDOM.nextInt(900000));

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("【OJ 智能学习平台】验证码");
            message.setText("您的验证码是：" + code + "\n\n验证码 5 分钟内有效，请勿泄露给他人。");
            mailSender.send(message);
            stringRedisTemplate.opsForValue().set(codeKey, code, CODE_TTL);
        } catch (RuntimeException e) {
            stringRedisTemplate.delete(rateLimitKey);
            throw e;
        }
    }

/**
 * 校验邮箱验证码是否有效。
 */
    @Override
    public boolean verifyCode(String email, String code) {
        String codeKey = CODE_KEY_PREFIX + email;
        String cachedCode = stringRedisTemplate.opsForValue().get(codeKey);
        if (cachedCode == null || !cachedCode.equals(code)) {
            return false;
        }
        stringRedisTemplate.delete(codeKey);
        return true;
    }
}
