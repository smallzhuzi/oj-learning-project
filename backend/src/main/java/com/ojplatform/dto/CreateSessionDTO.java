package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建练习会话请求 DTO
 * 用户从题库首页点击题目进入做题页面时调用
 */
public class CreateSessionDTO {

    /** 用户 ID（由后端从 JWT Token 中提取并设入） */
    private Long userId;

    /** 初始题目 slug（用户点击进入的那道题） */
    @NotBlank(message = "题目 slug 不能为空")
    private String problemSlug;

    /** OJ 平台标识（leetcode / luogu） */
    private String ojPlatform = "leetcode";

    // ==================== Getter / Setter ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProblemSlug() {
        return problemSlug;
    }

    public void setProblemSlug(String problemSlug) {
        this.problemSlug = problemSlug;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }
}
