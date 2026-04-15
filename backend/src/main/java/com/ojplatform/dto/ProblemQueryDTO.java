package com.ojplatform.dto;

import java.util.List;

/**
 * 题目查询数据传输对象。
 */
public class ProblemQueryDTO {

    /**
     * 关键字。
     */
    private String keyword;

    /**
     * 难度。
     */
    private String difficulty;

    /**
     * 在线判题平台。
     */
    private String ojPlatform = "leetcode";

    /**
     * 标签。
     */
    private List<String> tags;

    /**
     * 页码。
     */
    private Integer pageNum = 1;

    /**
     * 每页数量。
     */
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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
