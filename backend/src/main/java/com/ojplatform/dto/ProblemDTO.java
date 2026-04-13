package com.ojplatform.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.ojplatform.entity.Problem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目返回 DTO
 * 在保留原 Problem 主要字段的基础上，将 topicTags 统一为结构化标签列表。
 */
public class ProblemDTO {

    private Long id;
    private String slug;
    private String title;
    private String difficulty;
    private BigDecimal acceptanceRate;
    private String ojPlatform;
    private String contentMarkdown;

    @JsonRawValue
    private String codeSnippets;

    private List<ProblemTagDTO> topicTags;
    private String frontendId;
    private String questionId;
    private LocalDateTime createdAt;
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
