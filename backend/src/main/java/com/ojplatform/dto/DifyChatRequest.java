package com.ojplatform.dto;

/**
 * Dify 对话请求数据传输对象。
 */
public class DifyChatRequest {

    /**
     * 会话ID。
     */
    private Long sessionId;

    /**
     * 留言。
     */
    private String message;

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
     * 判题状态。
     */
    private String judgeStatus;

    /**
     * 运行耗时。
     */
    private String runtime;

    /**
     * 内存消耗。
     */
    private String memory;

    /**
     * 通过用例数。
     */
    private Integer totalCorrect;

    /**
     * 总用例数。
     */
    private Integer totalTestcases;

    /**
     * 提示等级。
     */
    private Integer hintLevel;

    /**
     * 在线判题平台。
     */
    private String ojPlatform = "leetcode";

    // ==================== Getter / Setter ====================

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getJudgeStatus() {
        return judgeStatus;
    }

    public void setJudgeStatus(String judgeStatus) {
        this.judgeStatus = judgeStatus;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public Integer getTotalCorrect() {
        return totalCorrect;
    }

    public void setTotalCorrect(Integer totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    public Integer getTotalTestcases() {
        return totalTestcases;
    }

    public void setTotalTestcases(Integer totalTestcases) {
        this.totalTestcases = totalTestcases;
    }

    public Integer getHintLevel() {
        return hintLevel;
    }

    public void setHintLevel(Integer hintLevel) {
        this.hintLevel = hintLevel;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }
}
