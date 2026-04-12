package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提交记录表实体类
 * 对应数据库表：submissions
 * 记录用户每次代码提交的详细信息和判题结果
 */
@TableName("submissions")
public class Submission implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 提交记录唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提交者用户 ID（关联 users.id） */
    private Long userId;

    /** 题目本地 ID（关联 problems.id） */
    private Long problemId;

    /** 所属练习会话 ID（关联 practice_sessions.id），可为空 */
    private Long sessionId;

    /** 编程语言（java / python3 / cpp 等） */
    private String language;

    /** 用户提交的源代码 */
    private String code;

    /** 判题结果状态（Pending / Accepted / Wrong Answer 等） */
    private String status;

    /** 运行耗时（如 "4 ms"） */
    private String runtime;

    /** 内存消耗（如 "39.2 MB"） */
    private String memory;

    /** 通过的测试用例数 */
    private Integer totalCorrect;

    /** 总测试用例数 */
    private Integer totalTestcases;

    /** 远程 OJ 返回的提交 ID（用于轮询判题结果） */
    private String remoteSubmissionId;

    /** 提交时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submittedAt;

    // ==================== Getter / Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getRemoteSubmissionId() {
        return remoteSubmissionId;
    }

    public void setRemoteSubmissionId(String remoteSubmissionId) {
        this.remoteSubmissionId = remoteSubmissionId;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
