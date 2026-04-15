package com.ojplatform.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 判题任务消息数据传输对象。
 */
public class JudgeTaskMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息 ID。
     */
    private String messageId;
    /**
     * 提交记录 ID。
     */
    private Long submissionId;
    /**
     * 任务类型。
     */
    private String taskType;
    /**
     * 重试次数。
     */
    private int attempt;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    public JudgeTaskMessage() {
    }

    public JudgeTaskMessage(String messageId, Long submissionId, String taskType, int attempt, LocalDateTime createdAt) {
        this.messageId = messageId;
        this.submissionId = submissionId;
        this.taskType = taskType;
        this.attempt = attempt;
        this.createdAt = createdAt;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
