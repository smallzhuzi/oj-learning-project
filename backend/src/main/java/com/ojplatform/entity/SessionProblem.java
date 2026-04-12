package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话题目关联表实体类
 * 对应数据库表：session_problems
 * 记录每个练习会话中用户经历的题目轨迹，
 * 通过 jumpType 区分进入方式，通过 seqOrder 保持题目顺序
 */
@TableName("session_problems")
public class SessionProblem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关联记录唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会话 ID（关联 practice_sessions.id） */
    private Long sessionId;

    /** 题目 ID（关联 problems.id） */
    private Long problemId;

    /**
     * 跳转类型枚举：
     * - initial：用户从题库首页直接点击进入
     * - next_recommend：Dify AI 推荐的下一题跳转
     */
    private String jumpType;

    /** 题目在会话中的顺序编号（从 1 开始递增） */
    private Integer seqOrder;

    /** 跳转时间 */
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
