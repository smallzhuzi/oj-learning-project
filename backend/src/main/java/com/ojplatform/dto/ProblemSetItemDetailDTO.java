package com.ojplatform.dto;

import java.math.BigDecimal;

/**
 * 题单题目详情 DTO
 * 用于返回题单内题目列表时携带题目基本信息
 */
public class ProblemSetItemDetailDTO {

    /** 关联记录 ID */
    private Long id;

    /** 题单 ID */
    private Long setId;

    /** 题目 ID */
    private Long problemId;

    /** 题目顺序 */
    private Integer seqOrder;

    /** 该题分值 */
    private Integer score;

    /** 题目 slug */
    private String slug;

    /** 题目标题 */
    private String title;

    /** LeetCode 前端题号 */
    private String frontendId;

    /** 难度 */
    private String difficulty;

    /** 通过率 */
    private BigDecimal acceptanceRate;

    /** 题目标签 JSON */
    private String topicTags;

    // ==================== Getter / Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSetId() {
        return setId;
    }

    public void setSetId(Long setId) {
        this.setId = setId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public Integer getSeqOrder() {
        return seqOrder;
    }

    public void setSeqOrder(Integer seqOrder) {
        this.seqOrder = seqOrder;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
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

    public String getFrontendId() {
        return frontendId;
    }

    public void setFrontendId(String frontendId) {
        this.frontendId = frontendId;
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

    public String getTopicTags() {
        return topicTags;
    }

    public void setTopicTags(String topicTags) {
        this.topicTags = topicTags;
    }
}
