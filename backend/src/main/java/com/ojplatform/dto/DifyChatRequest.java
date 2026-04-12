package com.ojplatform.dto;

/**
 * Dify 聊天请求 DTO
 * 前端向后端发起 Dify 对话时传入的参数
 */
public class DifyChatRequest {

    /** 练习会话 ID（用于关联 conversation_id） */
    private Long sessionId;

    /** 用户消息内容 */
    private String message;

    /** 当前题目 slug（推荐下一题 / 代码分析时使用） */
    private String problemSlug;

    /** 编程语言（代码分析时使用） */
    private String language;

    /** 用户代码（代码分析时使用） */
    private String code;

    /** 判题结果状态（代码分析时使用，如 Accepted / Wrong Answer） */
    private String judgeStatus;

    /** 运行耗时（代码分析时使用） */
    private String runtime;

    /** 内存消耗（代码分析时使用） */
    private String memory;

    /** 通过的测试用例数（代码分析时使用） */
    private Integer totalCorrect;

    /** 总测试用例数（代码分析时使用） */
    private Integer totalTestcases;

    /** 提示级别（渐进式提示时使用，1=思路方向, 2=关键步骤, 3=伪代码框架） */
    private Integer hintLevel;

    /** OJ 平台标识（leetcode / luogu），用于正确查询题目和指导 AI 推荐 */
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
