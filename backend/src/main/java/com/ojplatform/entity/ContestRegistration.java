package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 比赛报名表实体类
 * 对应数据库表：contest_registrations
 */
@TableName("contest_registrations")
public class ContestRegistration implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 报名记录唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 比赛 ID */
    private Long contestId;

    /** 用户 ID */
    private Long userId;

    /** 队伍 ID（组队赛时使用） */
    private Long teamId;

    /** 状态：registered / cancelled / disqualified */
    private String status;

    /** 报名时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime registeredAt;

    // ==================== Getter / Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
