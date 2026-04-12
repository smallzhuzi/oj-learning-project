package com.ojplatform.dto;

/**
 * LeetCode 提交代码响应 DTO
 * 对应 POST /problems/{slug}/submit/ 的返回结果
 */
public class LeetCodeSubmitResult {

    /** LeetCode 返回的远程提交 ID（用于后续轮询判题结果） */
    private String submissionId;

    public LeetCodeSubmitResult() {
    }

    public LeetCodeSubmitResult(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }
}
