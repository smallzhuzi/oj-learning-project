package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建比赛数据传输对象。
 */
public class CreateContestDTO {

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 标题。
     */
    @NotBlank(message = "比赛标题不能为空")
    private String title;

    /**
     * 描述。
     */
    private String description;

    /**
     * 比赛类型。
     */
    @NotBlank(message = "比赛类型不能为空")
    private String contestType;

    /**
     * 开始时间。
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 持续时间（分钟）。
     */
    @NotNull(message = "比赛时长不能为空")
    private Integer durationMinutes;

    /**
     * 封榜时长（分钟）。
     */
    private Integer freezeMinutes = 0;

    /**
     * 最大参赛人数。
     */
    private Integer maxParticipants = 0;

    /**
     * 最大队伍人数。
     */
    private Integer maxTeamSize = 3;

    /**
     * 最小队伍人数。
     */
    private Integer minTeamSize = 1;

    /**
     * 计分规则。
     */
    private String scoringRule = "acm";

    /**
     * 罚时（分钟）。
     */
    private Integer penaltyTime = 20;

    /**
     * 允许的编程语言。
     */
    private List<String> allowLanguage;

    /**
     * 是否公开。
     */
    private Boolean isPublic = true;

    /**
     * 密码。
     */
    private String password;

    /**
     * 在线判题平台。
     */
    private String ojPlatform = "leetcode";

    /**
     * 出题来源。
     */
    private String problemSource = "manual";

    /**
     * 题单ID。
     */
    private Long problemSetId;

    /**
     * 题目列表。
     */
    private List<ContestProblemItem> problems;

    /**
     * 是否立即发布。
     */
    private Boolean publish = false;

    /**
     * 比赛题目项数据传输对象。
     */
    public static class ContestProblemItem {
        /**
         * 题目标识。
         */
        private String slug;
        /**
         * 分数。
         */
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

    public String getContestType() {
        return contestType;
    }

    public void setContestType(String contestType) {
        this.contestType = contestType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getFreezeMinutes() {
        return freezeMinutes;
    }

    public void setFreezeMinutes(Integer freezeMinutes) {
        this.freezeMinutes = freezeMinutes;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(Integer maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public Integer getMinTeamSize() {
        return minTeamSize;
    }

    public void setMinTeamSize(Integer minTeamSize) {
        this.minTeamSize = minTeamSize;
    }

    public String getScoringRule() {
        return scoringRule;
    }

    public void setScoringRule(String scoringRule) {
        this.scoringRule = scoringRule;
    }

    public Integer getPenaltyTime() {
        return penaltyTime;
    }

    public void setPenaltyTime(Integer penaltyTime) {
        this.penaltyTime = penaltyTime;
    }

    public List<String> getAllowLanguage() {
        return allowLanguage;
    }

    public void setAllowLanguage(List<String> allowLanguage) {
        this.allowLanguage = allowLanguage;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }

    public String getProblemSource() {
        return problemSource;
    }

    public void setProblemSource(String problemSource) {
        this.problemSource = problemSource;
    }

    public Long getProblemSetId() {
        return problemSetId;
    }

    public void setProblemSetId(Long problemSetId) {
        this.problemSetId = problemSetId;
    }

    public List<ContestProblemItem> getProblems() {
        return problems;
    }

    public void setProblems(List<ContestProblemItem> problems) {
        this.problems = problems;
    }

    public Boolean getPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }
}
