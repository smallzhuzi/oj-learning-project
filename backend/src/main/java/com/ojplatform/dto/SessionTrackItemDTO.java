package com.ojplatform.dto;

import java.time.LocalDateTime;

/**
 * 会话轨迹条目数据传输对象。
 */
public class SessionTrackItemDTO {

    /**
     * 唯一标识。
     */
    private Long id;
    /**
     * 会话ID。
     */
    private Long sessionId;
    /**
     * 题目ID。
     */
    private Long problemId;
    /**
     * 跳转类型。
     */
    private String jumpType;
    /**
     * 顺序号。
     */
    private Integer seqOrder;
    /**
     * 跳转时间。
     */
    private LocalDateTime jumpedAt;

    // 以下字段来自 problems 表 JOIN
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

    // 以下字段来自 submissions 表子查询
    /**
     * 尝试次数。
     */
    private Integer attemptCount;
    /**
     * 是否通过。
     */
    private Boolean accepted;

    // ==================== Getter / Setter ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getProblemId() { return problemId; }
    public void setProblemId(Long problemId) { this.problemId = problemId; }

    public String getJumpType() { return jumpType; }
    public void setJumpType(String jumpType) { this.jumpType = jumpType; }

    public Integer getSeqOrder() { return seqOrder; }
    public void setSeqOrder(Integer seqOrder) { this.seqOrder = seqOrder; }

    public LocalDateTime getJumpedAt() { return jumpedAt; }
    public void setJumpedAt(LocalDateTime jumpedAt) { this.jumpedAt = jumpedAt; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFrontendId() { return frontendId; }
    public void setFrontendId(String frontendId) { this.frontendId = frontendId; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Integer getAttemptCount() { return attemptCount; }
    public void setAttemptCount(Integer attemptCount) { this.attemptCount = attemptCount; }

    public Boolean getAccepted() { return accepted; }
    public void setAccepted(Boolean accepted) { this.accepted = accepted; }
}
