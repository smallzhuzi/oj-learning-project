package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 题目实体类。
 */
@TableName("problems")
public class Problem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目标识。
     */
    private String slug;

    /**
     * 标题。
     */
    private String title;

    /**
     * 难度。
     */
    private String difficulty;

    /**
     * 通过率。
     */
    private BigDecimal acceptanceRate;

    /**
     * 在线判题平台。
     */
    private String ojPlatform;

    /**
     * Markdown 格式内容。
     */
    private String contentMarkdown;

    /**
     * 代码Snippets。
     */
    @JsonRawValue
    private String codeSnippets;

    /**
     * 主题标签。
     */
    @JsonRawValue
    private String topicTags;

    /**
     * 前端展示编号。
     */
    private String frontendId;

    /**
     * 远程题目ID。
     */
    private String questionId;

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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public BigDecimal getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(BigDecimal acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }

    public String getContentMarkdown() {
        return contentMarkdown;
    }

    public void setContentMarkdown(String contentMarkdown) {
        this.contentMarkdown = contentMarkdown;
    }

    @JsonRawValue
    public String getCodeSnippets() {
        return codeSnippets;
    }

    public void setCodeSnippets(String codeSnippets) {
        this.codeSnippets = codeSnippets;
    }

    @JsonRawValue
    public String getTopicTags() {
        return topicTags;
    }

    public void setTopicTags(String topicTags) {
        this.topicTags = topicTags;
    }

    public String getFrontendId() {
        return frontendId;
    }

    public void setFrontendId(String frontendId) {
        this.frontendId = frontendId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
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
