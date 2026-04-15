package com.ojplatform.service;

/**
 * 邮件相关业务接口。
 */
public interface EmailService {

    /**
     * 发送验证码到指定邮箱
     * @param email 目标邮箱
     */
    void sendVerificationCode(String email);

    /**
     * 校验验证码是否正确且未过期
     * @param email 邮箱
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String email, String code);
}
