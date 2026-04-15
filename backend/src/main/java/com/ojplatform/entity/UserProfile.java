package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户画像实体类。
 */
@TableName("user_profiles")
public class UserProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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

    /**
     * 擅长标签。
     */
    @JsonRawValue
    private String strongTags;

    /**
     * 薄弱标签。
     */
    @JsonRawValue
    private String weakTags;

    /**
     * 简单题通过数。
     */
    private Integer solvedEasy;

    /**
     * 中等题通过数。
     */
    private Integer solvedMedium;

    /**
     * 困难题通过数。
     */
    private Integer solvedHard;

    /**
     * 总Submissions。
     */
    private Integer totalSubmissions;

    /**
     * 通过率。
     */
    private BigDecimal acceptanceRate;

    /**
     * 上次分析时间。
     */
    private LocalDateTime lastAnalyzedAt;

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
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
