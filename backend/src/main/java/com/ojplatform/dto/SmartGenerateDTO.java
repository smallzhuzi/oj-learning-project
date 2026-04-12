package com.ojplatform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Dify 智能组题请求 DTO
 * 用户描述自己的水平和目标，由 Dify 智能推荐题目组合
 */
public class SmartGenerateDTO {

    /** 用户 ID（由后端从 JWT 中提取） */
    private Long userId;

    /** 题单标题（可选） */
    private String title;

    /** 题目数量 */
    @Min(value = 1, message = "至少 1 道题")
    @Max(value = 50, message = "最多 50 道题")
    private Integer count = 10;

    /** 自我定位描述（如"刷了50题Easy，Medium偶尔能做出来"） */
    @NotBlank(message = "请描述你的当前水平")
    private String selfAssessment;

    /** 目标要求描述（如"想要能稳定做出Medium，冲击Hard"） */
    @NotBlank(message = "请描述你的目标")
    private String targetGoal;

    /** 偏好方向（可选，如"想加强动态规划"） */
    private String preference;

    /** 时间预算描述（可选，如"2小时练习"） */
    private String timeBudget;

    /** OJ 平台标识 */
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
