package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户 OJ 配置数据传输对象。
 */
public class UserOjConfigDTO {

    /**
     * 在线判题平台。
     */
    @NotBlank(message = "OJ 平台不能为空")
    private String ojPlatform;

    /**
     * Cookie 内容。
     */
    private String cookieValue;

    /**
     * CSRF 令牌。
     */
    private String csrfToken;

    /**
     * 额外配置。
     */
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
