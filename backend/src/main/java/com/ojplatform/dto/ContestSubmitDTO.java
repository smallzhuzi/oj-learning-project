package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 比赛提交数据传输对象。
 */
public class ContestSubmitDTO {

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 比赛ID。
     */
    private Long contestId;

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

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

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

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }
}
