package com.ojplatform.dto;

import java.math.BigDecimal;

/**
 * 题单条目详情数据传输对象。
 */
public class ProblemSetItemDetailDTO {

    /**
     * 唯一标识。
     */
    private Long id;

    /**
     * 题单 ID。
     */
    private Long setId;

    /**
     * 题目ID。
     */
    private Long problemId;

    /**
     * 顺序号。
     */
    private Integer seqOrder;

    /**
     * 分数。
     */
    private Integer score;

    /**
     * 题目标识。
     */
    private String slug;

    /**
     * 标题。
     */
    private String title;

    /**
     * 前端展示编号。
     */
    private String frontendId;

    /**
     * 难度。
     */
    private String difficulty;

    /**
     * 通过率。
     */
    private BigDecimal acceptanceRate;

    /**
     * 主题标签。
     */
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
