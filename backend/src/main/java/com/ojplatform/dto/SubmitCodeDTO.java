package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 提交代码请求 DTO
 * 前端提交代码时传入的参数
 */
public class SubmitCodeDTO {

    /** 题目 slug（如 two-sum） */
    @NotBlank(message = "题目 slug 不能为空")
    private String problemSlug;

    /** 编程语言（java / python3 / cpp） */
    @NotBlank(message = "编程语言不能为空")
    private String language;

    /** 用户代码 */
    @NotBlank(message = "代码不能为空")
    private String code;

    /** OJ 平台标识（leetcode / luogu） */
    private String ojPlatform = "leetcode";

    /** 当前会话 ID（可选） */
    private Long sessionId;

    /** 用户 ID（由后端从 JWT Token 中提取并设入） */
    private Long userId;

    // ==================== Getter / Setter ====================

    public String getProblemSlug() {
        return problemSlug;
    }

    public void setProblemSlug(String problemSlug) {
        this.problemSlug = problemSlug;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }
}
