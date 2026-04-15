package com.ojplatform.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * OJ 题目详情数据传输对象。
 */
public class OjProblemDetail {

    /**
     * 远程题目ID。
     */
    private String questionId;

    /**
     * 前端展示编号。
     */
    private String frontendId;

    /**
     * 标题。
     */
    private String title;

    /**
     * 内容。
     */
    private String content;

    /**
     * 难度。
     */
    private String difficulty;

    /**
     * 通过率。
     */
    private BigDecimal acceptanceRate;

    /**
     * 代码Snippets。
     */
    private List<LeetCodeProblemDetail.CodeSnippet> codeSnippets;

    /**
     * 主题标签。
     */
    private List<LeetCodeProblemDetail.TopicTag> topicTags;

    /**
     * 统计。
     */
    private String stats;

    // ==================== Getter / Setter ====================

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getFrontendId() {
        return frontendId;
    }

    public void setFrontendId(String frontendId) {
        this.frontendId = frontendId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public List<LeetCodeProblemDetail.CodeSnippet> getCodeSnippets() {
        return codeSnippets;
    }

    public void setCodeSnippets(List<LeetCodeProblemDetail.CodeSnippet> codeSnippets) {
        this.codeSnippets = codeSnippets;
    }

    public List<LeetCodeProblemDetail.TopicTag> getTopicTags() {
        return topicTags;
    }

    public void setTopicTags(List<LeetCodeProblemDetail.TopicTag> topicTags) {
        this.topicTags = topicTags;
    }

    public String getStats() {
        return stats;
    }

    public void setStats(String stats) {
        this.stats = stats;
    }
}
