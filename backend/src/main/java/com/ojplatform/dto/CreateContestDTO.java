package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建比赛请求 DTO
 */
public class CreateContestDTO {

    /** 创建者用户 ID（由后端从 JWT 中提取） */
    private Long userId;

    /** 比赛标题 */
    @NotBlank(message = "比赛标题不能为空")
    private String title;

    /** 比赛说明 */
    private String description;

    /** 比赛类型：individual / team */
    @NotBlank(message = "比赛类型不能为空")
    private String contestType;

    /** 开始时间 */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /** 比赛时长（分钟） */
    @NotNull(message = "比赛时长不能为空")
    private Integer durationMinutes;

    /** 封榜时间（分钟，0 = 不封榜） */
    private Integer freezeMinutes = 0;

    /** 最大参赛人数（0 = 不限） */
    private Integer maxParticipants = 0;

    /** 组队赛最大队伍人数 */
    private Integer maxTeamSize = 3;

    /** 计分规则：acm / oi / cf */
    private String scoringRule = "acm";

    /** ACM 罚时分钟数 */
    private Integer penaltyTime = 20;

    /** 允许的编程语言 */
    private List<String> allowLanguage;

    /** 是否公开 */
    private Boolean isPublic = true;

    /** 私有比赛密码 */
    private String password;

    /** OJ 平台标识 */
    private String ojPlatform = "leetcode";

    /** 出题方式：manual（手动选题）/ existing_set（已有题单）/ auto（自动组题） */
    private String problemSource = "manual";

    /** 已有题单 ID（problemSource=existing_set 时使用） */
    private Long problemSetId;

    /** 手动选题的题目 slug 列表（problemSource=manual 时使用） */
    private List<ContestProblemItem> problems;

    /**
     * 比赛题目项
     */
    public static class ContestProblemItem {
        private String slug;
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
}
