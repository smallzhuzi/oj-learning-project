package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * 创建题单数据传输对象。
 */
public class CreateProblemSetDTO {

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 标题。
     */
    @NotBlank(message = "题单标题不能为空")
    private String title;

    /**
     * 描述。
     */
    private String description;

    /**
     * 难度等级。
     */
    private String difficultyLevel;

    /**
     * 在线判题平台。
     */
    private String ojPlatform = "leetcode";

    /**
     * 题目列表。
     */
    private List<ProblemItem> problems;

    /**
     * 题目条目数据传输对象。
     */
    public static class ProblemItem {
        /**
         * 题目标识。
         */
        @NotBlank(message = "题目 slug 不能为空")
        private String slug;

        /**
         * 分数。
         */
        @Min(value = 1, message = "分值至少为 1")
        private Integer score = 100;

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }

    public List<ProblemItem> getProblems() {
        return problems;
    }

    public void setProblems(List<ProblemItem> problems) {
        this.problems = problems;
    }
}
