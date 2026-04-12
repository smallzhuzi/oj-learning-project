package com.ojplatform.dto;

import java.time.LocalDateTime;

/**
 * 轨迹链概要 DTO
 * 用于侧边栏列表展示每条轨迹链的头题信息和题目数量
 */
public class SessionChainDTO {

    /** 会话 ID（即轨迹链 ID） */
    private Long sessionId;

    /** Dify 对话 ID */
    private String difyConversationId;

    /** 会话开始时间 */
    private LocalDateTime startedAt;

    /** 头题 slug */
    private String headSlug;

    /** 头题标题 */
    private String headTitle;

    /** 头题题号 */
    private String headFrontendId;

    /** 头题难度 */
    private String headDifficulty;

    /** 链上题目总数 */
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
