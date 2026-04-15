package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 保存草稿数据传输对象。
 */
public class SaveDraftDTO {

    /**
     * 题目标识。
     */
    @NotBlank(message = "题目 slug 不能为空")
    private String problemSlug;

    /**
     * 编程语言。
     */
    @NotBlank(message = "编程语言不能为空")
    private String language;

    /**
     * 代码内容。
     */
    @NotBlank(message = "代码不能为空")
    private String code;

    /**
     * 用户ID。
     */
    private Long userId;

    // ==================== Getter / Setter ====================

    public String getProblemSlug() { return problemSlug; }
    public void setProblemSlug(String problemSlug) { this.problemSlug = problemSlug; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
