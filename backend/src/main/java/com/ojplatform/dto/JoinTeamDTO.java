package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 加入队伍请求 DTO
 */
public class JoinTeamDTO {

    /** 用户 ID（由后端从 JWT 中提取） */
    private Long userId;

    /** 邀请码 */
    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;

    // ==================== Getter / Setter ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }
}
