package com.ojplatform.dto;

/**
 * 力扣提交结果数据传输对象。
 */
public class LeetCodeSubmitResult {

    /**
     * 提交记录 ID。
     */
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
