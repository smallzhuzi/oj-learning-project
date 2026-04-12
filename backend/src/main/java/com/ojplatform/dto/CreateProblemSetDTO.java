package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;

/**
 * 创建题单请求 DTO（手动选题）
 */
public class CreateProblemSetDTO {

    /** 用户 ID（由后端从 JWT 中提取） */
    private Long userId;

    /** 题单标题 */
    @NotBlank(message = "题单标题不能为空")
    private String title;

    /** 题单描述 */
    private String description;

    /** 整体难度定位 */
    private String difficultyLevel;

    /** OJ 平台标识 */
    private String ojPlatform = "leetcode";

    /** 初始题目列表（题目 slug 数组，可选） */
    private List<ProblemItem> problems;

    /**
     * 题目项（slug + 分值）
     */
    public static class ProblemItem {
        @NotBlank(message = "题目 slug 不能为空")
        private String slug;

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
