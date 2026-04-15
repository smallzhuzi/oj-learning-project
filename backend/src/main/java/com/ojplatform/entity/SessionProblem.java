package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话题目实体类。
 */
@TableName("session_problems")
public class SessionProblem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识。
     */
    @TableId(type = IdType.AUTO)
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime jumpedAt;

    // ==================== Getter / Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getJumpType() {
        return jumpType;
    }

    public void setJumpType(String jumpType) {
        this.jumpType = jumpType;
    }

    public Integer getSeqOrder() {
        return seqOrder;
    }

    public void setSeqOrder(Integer seqOrder) {
        this.seqOrder = seqOrder;
    }

    public LocalDateTime getJumpedAt() {
        return jumpedAt;
    }

    public void setJumpedAt(LocalDateTime jumpedAt) {
        this.jumpedAt = jumpedAt;
    }
}
