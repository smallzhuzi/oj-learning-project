package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题单表实体类
 * 对应数据库表：problem_sets
 * 存储用户或系统组好的一套题目集合
 */
@TableName("problem_sets")
public class ProblemSet implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 题单唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建者用户 ID */
    private Long userId;

    /** 题单标题 */
    private String title;

    /** 题单描述 */
    private String description;

    /** 来源类型：manual / quick / dify_smart */
    private String sourceType;

    /** 整体难度定位：beginner / intermediate / advanced / custom */
    private String difficultyLevel;

    /** 题目数量 */
    private Integer problemCount;

    /** 总分 */
    private Integer totalScore;

    /** 标签/知识点 JSON 数组 */
    @JsonRawValue
    private String tags;

    /** Dify 智能组题时的输入参数快照 */
    @JsonRawValue
    private String difyParams;

    /** 可见性：private / public / contest_only */
    private String visibility;

    /** 状态：draft / published / archived */
    private String status;

    /** OJ 平台标识 */
    private String ojPlatform;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 最后更新时间 */
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Integer getProblemCount() {
        return problemCount;
    }

    public void setProblemCount(Integer problemCount) {
        this.problemCount = problemCount;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    @JsonRawValue
    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    @JsonRawValue
    public String getDifyParams() {
        return difyParams;
    }

    public void setDifyParams(String difyParams) {
        this.difyParams = difyParams;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
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
