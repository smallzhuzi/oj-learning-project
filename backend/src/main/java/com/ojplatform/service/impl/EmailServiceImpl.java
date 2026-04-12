package com.ojplatform.service.impl;

import com.ojplatform.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮件验证码服务实现
 * 使用 ConcurrentHashMap 存储验证码，5 分钟有效，60 秒防刷
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /** 验证码存储：邮箱 → CodeEntry */
    private final ConcurrentHashMap<String, CodeEntry> codeMap = new ConcurrentHashMap<>();

    /** 验证码有效期 5 分钟 */
    private static final long CODE_EXPIRE_MS = 5 * 60 * 1000;
    /** 发送间隔 60 秒 */
    private static final long SEND_INTERVAL_MS = 60 * 1000;

    @Override
    public void sendVerificationCode(String email) {
        // 防刷：60 秒内不允许重复发送
        CodeEntry existing = codeMap.get(email);
        if (existing != null && System.currentTimeMillis() - existing.createdAt < SEND_INTERVAL_MS) {
            throw new RuntimeException("发送太频繁，请 60 秒后再试");
        }

        // 生成 6 位随机验证码
        String code = String.valueOf((int) ((Math.random() * 900000) + 100000));

        // 存储验证码
        codeMap.put(email, new CodeEntry(code, System.currentTimeMillis()));

        // 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("【OJ 智能学习平台】验证码");
        message.setText("您的验证码是：" + code + "\n\n验证码 5 分钟内有效，请勿泄露给他人。");
        mailSender.send(message);
    }

    @Override
    public boolean verifyCode(String email, String code) {
        CodeEntry entry = codeMap.get(email);
        if (entry == null) {
            return false;
        }
        // 判断是否过期
        if (System.currentTimeMillis() - entry.createdAt > CODE_EXPIRE_MS) {
            codeMap.remove(email);
            return false;
        }
        // 判断是否匹配
        if (!entry.code.equals(code)) {
            return false;
        }
        // 验证成功后删除（一次性使用）
        codeMap.remove(email);
        return true;
    }

    /** 验证码条目 */
    private static class CodeEntry {
        final String code;
        final long createdAt;

        CodeEntry(String code, long createdAt) {
            this.code = code;
            this.createdAt = createdAt;
        }
    }
}
