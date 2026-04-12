package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 题目搜索请求 DTO
 * 支持按题号或标题模糊搜索，支持按难度筛选
 */
public class ProblemQueryDTO {

    /** 搜索关键词（题号或标题，可选） */
    private String keyword;

    /** 难度筛选（Easy / Medium / Hard，可选） */
    private String difficulty;

    /** OJ 平台（默认 leetcode） */
    private String ojPlatform = "leetcode";

    /** 当前页码（从 1 开始） */
    private Integer pageNum = 1;

    /** 每页条数（默认 20） */
    private Integer pageSize = 20;

    // ==================== Getter / Setter ====================

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
