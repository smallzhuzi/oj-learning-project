package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 练习会话实体类。
 */
@TableName("practice_sessions")
public class PracticeSession implements Serializable {

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
     * Dify 会话标识。
     */
    private String difyConversationId;

    /**
     * 开始时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime startedAt;

    /**
     * 最后活跃时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime lastActiveAt;

    /**
     * 结束时间。
     */
    private LocalDateTime endedAt;

    // ==================== Getter / Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getDifyConversationId() {
        return difyConversationId;
    }

    public void setDifyConversationId(String difyConversationId) {
        this.difyConversationId = difyConversationId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }
}
