package com.ojplatform.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 平台无关的题目详情 DTO
 * 统一不同 OJ 平台（LeetCode、洛谷等）的题目信息格式
 */
public class OjProblemDetail {

    /** 平台内部题目 ID（LeetCode: questionId / 洛谷: pid） */
    private String questionId;

    /** 前端展示的题号（LeetCode: "1" / 洛谷: "P1001"） */
    private String frontendId;

    /** 题目标题 */
    private String title;

    /** 题目描述内容（LeetCode: HTML / 洛谷: Markdown） */
    private String content;

    /** 统一难度（Easy / Medium / Hard） */
    private String difficulty;

    /** 通过率（百分比，如 49.5） */
    private BigDecimal acceptanceRate;

    /** 各语言代码模板（洛谷为空列表） */
    private List<LeetCodeProblemDetail.CodeSnippet> codeSnippets;

    /** 题目标签列表 */
    private List<LeetCodeProblemDetail.TopicTag> topicTags;

    /** 原始统计信息 JSON 字符串 */
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
