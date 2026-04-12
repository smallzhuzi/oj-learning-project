package com.ojplatform.dto;

/**
 * 更新用户画像请求 DTO
 */
public class UpdateUserProfileDTO {

    /** 用户 ID（由后端从 JWT 中提取） */
    private Long userId;

    /** 自评水平：beginner / intermediate / advanced / expert */
    private String skillLevel;

    /** 目标水平 */
    private String targetLevel;

    // ==================== Getter / Setter ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) {
        this.skillLevel = skillLevel;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(String targetLevel) {
        this.targetLevel = targetLevel;
    }
}
