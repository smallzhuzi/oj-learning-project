package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * OJ 平台配置请求 DTO
 */
public class UserOjConfigDTO {

    @NotBlank(message = "OJ 平台不能为空")
    private String ojPlatform;

    private String cookieValue;

    private String csrfToken;

    private String extraConfig;

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }

    public String getCookieValue() {
        return cookieValue;
    }

    public void setCookieValue(String cookieValue) {
        this.cookieValue = cookieValue;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }

    public String getExtraConfig() {
        return extraConfig;
    }

    public void setExtraConfig(String extraConfig) {
        this.extraConfig = extraConfig;
    }
}
