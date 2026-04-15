package com.ojplatform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 智能生成数据传输对象。
 */
public class SmartGenerateDTO {

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 标题。
     */
    private String title;

    /**
     * 数量。
     */
    @Min(value = 1, message = "至少 1 道题")
    @Max(value = 50, message = "最多 50 道题")
    private Integer count = 10;

    /**
     * 自我评估。
     */
    @NotBlank(message = "请描述你的当前水平")
    private String selfAssessment;

    /**
     * 目标Goal。
     */
    @NotBlank(message = "请描述你的目标")
    private String targetGoal;

    /**
     * 偏好设置。
     */
    private String preference;

    /**
     * 时间Budget。
     */
    private String timeBudget;

    /**
     * 在线判题平台。
     */
    private String ojPlatform = "leetcode";

    // ==================== Getter / Setter ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getSelfAssessment() {
        return selfAssessment;
    }

    public void setSelfAssessment(String selfAssessment) {
        this.selfAssessment = selfAssessment;
    }

    public String getTargetGoal() {
        return targetGoal;
    }

    public void setTargetGoal(String targetGoal) {
        this.targetGoal = targetGoal;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }

    public String getTimeBudget() {
        return timeBudget;
    }

    public void setTimeBudget(String timeBudget) {
        this.timeBudget = timeBudget;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }
}
