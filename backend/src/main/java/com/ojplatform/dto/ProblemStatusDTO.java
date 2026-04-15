package com.ojplatform.dto;

/**
 * 题目状态数据传输对象。
 */
public class ProblemStatusDTO {

    /**
     * 题目标识。
     */
    private String slug;
    /**
     * 状态。
     */
    private String status;

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
