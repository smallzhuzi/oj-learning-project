package com.ojplatform.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * 快速生成数据传输对象。
 */
public class QuickGenerateDTO {

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 标题。
     */
    private String title;

    /**
     * 数量。
     */
    @Min(value = 1, message = "至少 1 道题")
    @Max(value = 50, message = "最多 50 道题")
    private Integer count = 10;

    /**
     * 难度等级。
     */
    private String difficultyLevel = "intermediate";

    /**
     * 难度分布。
     */
    private DifficultyDistribution distribution;

    /**
     * 标签。
     */
    private List<String> tags;

    /**
     * 是否排除已通过题目。
     */
    private Boolean excludeSolved = true;

    /**
     * 在线判题平台。
     */
    private String ojPlatform = "leetcode";

    /**
     * 难度Distribution数据传输对象。
     */
    public static class DifficultyDistribution {
        /**
         * 简单题数量。
         */
        private Integer easy = 0;
        /**
         * 中等题数量。
         */
        private Integer medium = 0;
        /**
         * 困难题数量。
         */
        private Integer hard = 0;

        public Integer getEasy() {
            return easy;
        }

        public void setEasy(Integer easy) {
            this.easy = easy;
        }

        public Integer getMedium() {
            return medium;
        }

        public void setMedium(Integer medium) {
            this.medium = medium;
        }

        public Integer getHard() {
            return hard;
        }

        public void setHard(Integer hard) {
            this.hard = hard;
        }
    }

    // ==================== Getter / Setter ====================

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public DifficultyDistribution getDistribution() {
        return distribution;
    }

    public void setDistribution(DifficultyDistribution distribution) {
        this.distribution = distribution;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Boolean getExcludeSolved() {
        return excludeSolved;
    }

    public void setExcludeSolved(Boolean excludeSolved) {
        this.excludeSolved = excludeSolved;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }
}
