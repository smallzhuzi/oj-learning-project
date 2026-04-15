package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建会话数据传输对象。
 */
public class CreateSessionDTO {

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 题目标识。
     */
    @NotBlank(message = "题目 slug 不能为空")
    private String problemSlug;

    /**
     * 在线判题平台。
     */
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
