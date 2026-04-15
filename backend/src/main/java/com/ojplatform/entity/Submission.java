package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提交记录实体类。
 */
@TableName("submissions")
public class Submission implements Serializable {

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
     * 题目ID。
     */
    private Long problemId;

    /**
     * 会话ID。
     */
    private Long sessionId;

    /**
     * 编程语言。
     */
    private String language;

    /**
     * 代码内容。
     */
    private String code;

    /**
     * 状态。
     */
    private String status;

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
     * 远程提交ID。
     */
    private String remoteSubmissionId;

    /**
     * 提交时间。
     */
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
