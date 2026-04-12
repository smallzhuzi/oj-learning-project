package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 练习会话表实体类
 * 对应数据库表：practice_sessions
 * 每次从题库首页进入做题页面时创建一个新会话，
 * 会话绑定 Dify 对话 ID，追踪用户完整学习路径
 */
@TableName("practice_sessions")
public class PracticeSession implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 会话唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID（关联 users.id） */
    private Long userId;

    /** Dify 对话 ID（首次与 Dify 交互时由 Dify 返回并绑定） */
    private String difyConversationId;

    /** 会话开始时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime startedAt;

    /** 最后活跃时间（用于排序） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime lastActiveAt;

    /** 会话结束时间（用户关闭页面或手动结束时更新） */
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
