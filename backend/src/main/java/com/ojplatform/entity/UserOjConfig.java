package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 OJ 配置实体类。
 */
@TableName("user_oj_configs")
public class UserOjConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 在线判题平台。
     */
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

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ==================== Getter / Setter ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOjPlatform() { return ojPlatform; }
    public void setOjPlatform(String ojPlatform) { this.ojPlatform = ojPlatform; }

    public String getCookieValue() { return cookieValue; }
    public void setCookieValue(String cookieValue) { this.cookieValue = cookieValue; }

    public String getCsrfToken() { return csrfToken; }
    public void setCsrfToken(String csrfToken) { this.csrfToken = csrfToken; }

    public String getExtraConfig() { return extraConfig; }
    public void setExtraConfig(String extraConfig) { this.extraConfig = extraConfig; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
