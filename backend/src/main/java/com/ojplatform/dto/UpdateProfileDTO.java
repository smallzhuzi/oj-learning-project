package com.ojplatform.dto;

import jakarta.validation.constraints.Email;

/**
 * 更新资料数据传输对象。
 */
public class UpdateProfileDTO {

    /**
     * 邮箱。
     */
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 代码内容。
     */
    private String code;

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
}
