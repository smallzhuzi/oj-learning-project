package com.ojplatform.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.ojplatform.entity.Problem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目数据传输对象。
 */
public class ProblemDTO {

    /**
     * 唯一标识。
     */
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
    private List<ProblemTagDTO> topicTags;
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
    private LocalDateTime createdAt;
    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    public static ProblemDTO fromProblem(Problem problem, List<ProblemTagDTO> topicTags) {
        ProblemDTO dto = new ProblemDTO();
        dto.setId(problem.getId());
        dto.setSlug(problem.getSlug());
        dto.setTitle(problem.getTitle());
        dto.setDifficulty(problem.getDifficulty());
        dto.setAcceptanceRate(problem.getAcceptanceRate());
        dto.setOjPlatform(problem.getOjPlatform());
        dto.setContentMarkdown(problem.getContentMarkdown());
        dto.setCodeSnippets(problem.getCodeSnippets());
        dto.setTopicTags(topicTags);
        dto.setFrontendId(problem.getFrontendId());
        dto.setQuestionId(problem.getQuestionId());
        dto.setCreatedAt(problem.getCreatedAt());
        dto.setUpdatedAt(problem.getUpdatedAt());
        return dto;
    }

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

    public List<ProblemTagDTO> getTopicTags() {
        return topicTags;
    }

    public void setTopicTags(List<ProblemTagDTO> topicTags) {
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
