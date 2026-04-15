package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 入队申请实体类。
 */
@TableName("team_join_requests")
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 队伍ID。
     */
    private Long teamId;

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 留言。
     */
    private String message;

    /**
     * 状态。
     */
    private String status;

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ==================== Getter / Setter ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
