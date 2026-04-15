package com.ojplatform.dto;

import java.time.LocalDateTime;

/**
 * 会话链路数据传输对象。
 */
public class SessionChainDTO {

    /**
     * 会话ID。
     */
    private Long sessionId;

    /**
     * Dify 会话标识。
     */
    private String difyConversationId;

    /**
     * 开始时间。
     */
    private LocalDateTime startedAt;

    /**
     * 头节点标识。
     */
    private String headSlug;

    /**
     * 头节点标题。
     */
    private String headTitle;

    /**
     * 头节点前端ID。
     */
    private String headFrontendId;

    /**
     * 头节点难度。
     */
    private String headDifficulty;

    /**
     * 题目数量。
     */
    private Integer problemCount;

    // ==================== Getter / Setter ====================

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getDifyConversationId() { return difyConversationId; }
    public void setDifyConversationId(String difyConversationId) { this.difyConversationId = difyConversationId; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public String getHeadSlug() { return headSlug; }
    public void setHeadSlug(String headSlug) { this.headSlug = headSlug; }

    public String getHeadTitle() { return headTitle; }
    public void setHeadTitle(String headTitle) { this.headTitle = headTitle; }

    public String getHeadFrontendId() { return headFrontendId; }
    public void setHeadFrontendId(String headFrontendId) { this.headFrontendId = headFrontendId; }

    public String getHeadDifficulty() { return headDifficulty; }
    public void setHeadDifficulty(String headDifficulty) { this.headDifficulty = headDifficulty; }

    public Integer getProblemCount() { return problemCount; }
    public void setProblemCount(Integer problemCount) { this.problemCount = problemCount; }
}
