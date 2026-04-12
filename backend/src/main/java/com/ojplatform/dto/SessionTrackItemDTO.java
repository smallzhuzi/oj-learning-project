package com.ojplatform.dto;

import java.time.LocalDateTime;

/**
 * 会话轨迹项 DTO（关联题目详情）
 * 用于前端侧边栏展示题号、标题、难度等信息
 */
public class SessionTrackItemDTO {

    private Long id;
    private Long sessionId;
    private Long problemId;
    private String jumpType;
    private Integer seqOrder;
    private LocalDateTime jumpedAt;

    // 以下字段来自 problems 表 JOIN
    private String slug;
    private String title;
    private String frontendId;
    private String difficulty;

    // 以下字段来自 submissions 表子查询
    /** 该题在该会话内的提交次数 */
    private Integer attemptCount;
    /** 该题在该会话内是否已通过（AC） */
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
