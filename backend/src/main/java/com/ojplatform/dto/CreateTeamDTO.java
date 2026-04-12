package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建队伍请求 DTO
 */
public class CreateTeamDTO {

    /** 用户 ID（由后端从 JWT 中提取，自动成为队长） */
    private Long userId;

    /** 队伍名称 */
    @NotBlank(message = "队伍名称不能为空")
    private String teamName;

    // ==================== Getter / Setter ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }
}
