package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 题目缓存表实体类
 * 对应数据库表：problems
 * 缓存从远程 OJ 拉取的题目信息，避免频繁请求远程接口
 */
@TableName("problems")
public class Problem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 题目本地唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题目 slug（如 two-sum），用于构造 URL 和 API 调用 */
    private String slug;

    /** 题目标题（如"两数之和"） */
    private String title;

    /** 难度等级：Easy / Medium / Hard */
    private String difficulty;

    /** 通过率（百分比，如 49.50 表示 49.50%） */
    private BigDecimal acceptanceRate;

    /** OJ 平台标识（leetcode / codeforces 等） */
    private String ojPlatform;

    /** 题目描述（Markdown/HTML），从远程拉取后缓存 */
    private String contentMarkdown;

    /** 各语言初始代码模板（JSON 数组字符串） */
    @JsonRawValue
    private String codeSnippets;

    /** 题目标签（JSON 数组字符串，如 [{"name":"数组","slug":"array"}]） */
    @JsonRawValue
    private String topicTags;

    /** LeetCode 前端展示的题号（如 "1"、"2"） */
    private String frontendId;

    /** LeetCode 内部题目 ID（提交代码时需要） */
    private String questionId;

    /** 首次入库时间 */
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
