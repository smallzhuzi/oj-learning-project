package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 题单题目关联表实体类
 * 对应数据库表：problem_set_items
 * 题单内包含的题目列表
 */
@TableName("problem_set_items")
public class ProblemSetItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 关联记录唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题单 ID */
    private Long setId;

    /** 题目 ID */
    private Long problemId;

    /** 题目在题单中的顺序 */
    private Integer seqOrder;

    /** 该题分值 */
    private Integer score;

    /** 添加时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
