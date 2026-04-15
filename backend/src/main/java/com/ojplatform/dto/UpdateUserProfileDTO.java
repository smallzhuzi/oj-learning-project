package com.ojplatform.dto;

/**
 * 更新用户画像数据传输对象。
 */
public class UpdateUserProfileDTO {

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 技能等级。
     */
    private String skillLevel;

    /**
     * 目标等级。
     */
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
