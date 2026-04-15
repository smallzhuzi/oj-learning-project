package com.ojplatform.dto;

import java.time.LocalDateTime;

/**
 * 比赛队伍成员数据传输对象。
 */
public class ContestTeamMemberDTO {

    /**
     * 用户ID。
     */
    private Long userId;
    /**
     * 用户名。
     */
    private String username;
    /**
     * 角色。
     */
    private String role;
    /**
     * 加入时间。
     */
    private LocalDateTime joinedAt;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
