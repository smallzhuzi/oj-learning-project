package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 队伍成员表实体类
 * 对应数据库表：contest_team_members
 */
@TableName("contest_team_members")
public class ContestTeamMember implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 记录唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 队伍 ID */
    private Long teamId;

    /** 用户 ID */
    private Long userId;

    /** 角色：captain / member */
    private String role;

    /** 加入时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime joinedAt;

    // ==================== Getter / Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
