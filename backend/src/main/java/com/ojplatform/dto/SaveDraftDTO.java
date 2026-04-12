package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 保存代码草稿请求 DTO
 */
public class SaveDraftDTO {

    @NotBlank(message = "题目 slug 不能为空")
    private String problemSlug;

    @NotBlank(message = "编程语言不能为空")
    private String language;

    @NotBlank(message = "代码不能为空")
    private String code;

    /** 用户 ID（由后端从 JWT Token 中提取并设入） */
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
