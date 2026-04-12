package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户画像表实体类
 * 对应数据库表：user_profiles
 * 记录用户的能力水平、做题偏好、薄弱项等
 */
@TableName("user_profiles")
public class UserProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 画像唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 自评水平：beginner / intermediate / advanced / expert */
    private String skillLevel;

    /** 目标水平 */
    private String targetLevel;

    /** 擅长领域 JSON 数组 */
    @JsonRawValue
    private String strongTags;

    /** 薄弱领域 JSON 数组 */
    @JsonRawValue
    private String weakTags;

    /** 已解决 Easy 题目数 */
    private Integer solvedEasy;

    /** 已解决 Medium 题目数 */
    private Integer solvedMedium;

    /** 已解决 Hard 题目数 */
    private Integer solvedHard;

    /** 总提交次数 */
    private Integer totalSubmissions;

    /** 个人通过率 */
    private BigDecimal acceptanceRate;

    /** 上次画像分析时间 */
    private LocalDateTime lastAnalyzedAt;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ==================== Getter / Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    @JsonRawValue
    public String getStrongTags() {
        return strongTags;
    }

    public void setStrongTags(String strongTags) {
        this.strongTags = strongTags;
    }

    @JsonRawValue
    public String getWeakTags() {
        return weakTags;
    }

    public void setWeakTags(String weakTags) {
        this.weakTags = weakTags;
    }

    public Integer getSolvedEasy() {
        return solvedEasy;
    }

    public void setSolvedEasy(Integer solvedEasy) {
        this.solvedEasy = solvedEasy;
    }

    public Integer getSolvedMedium() {
        return solvedMedium;
    }

    public void setSolvedMedium(Integer solvedMedium) {
        this.solvedMedium = solvedMedium;
    }

    public Integer getSolvedHard() {
        return solvedHard;
    }

    public void setSolvedHard(Integer solvedHard) {
        this.solvedHard = solvedHard;
    }

    public Integer getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(Integer totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public BigDecimal getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(BigDecimal acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public LocalDateTime getLastAnalyzedAt() {
        return lastAnalyzedAt;
    }

    public void setLastAnalyzedAt(LocalDateTime lastAnalyzedAt) {
        this.lastAnalyzedAt = lastAnalyzedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
