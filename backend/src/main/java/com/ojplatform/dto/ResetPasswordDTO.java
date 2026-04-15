package com.ojplatform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重置密码数据传输对象。
 */
public class ResetPasswordDTO {

    /**
     * 邮箱。
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 代码内容。
     */
    @NotBlank(message = "验证码不能为空")
    private String code;

    /**
     * 新密码。
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度 6-50 个字符")
    private String newPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
