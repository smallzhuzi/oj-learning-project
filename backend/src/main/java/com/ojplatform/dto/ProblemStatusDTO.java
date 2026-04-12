package com.ojplatform.dto;

/**
 * 题目提交状态 DTO（用于题库首页批量查询）
 * slug → status (accepted / attempted)
 */
public class ProblemStatusDTO {

    private String slug;
    /** accepted = 有 AC 提交；attempted = 有提交但未 AC */
    private String status;

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
