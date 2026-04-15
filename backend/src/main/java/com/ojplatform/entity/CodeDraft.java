package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 代码草稿实体类。
 */
@TableName("code_drafts")
public class CodeDraft implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 题目标识。
     */
    private String problemSlug;

    /**
     * 编程语言。
     */
    private String language;

    /**
     * 代码内容。
     */
    private String code;

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ==================== Getter / Setter ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getProblemSlug() { return problemSlug; }
    public void setProblemSlug(String problemSlug) { this.problemSlug = problemSlug; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
