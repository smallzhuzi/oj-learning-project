package com.ojplatform.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 比赛详情数据传输对象。
 */
public class ContestDetailDTO {

    /**
     * 唯一标识。
     */
    private Long id;

    /**
     * 创建者名称。
     */
    private String creatorName;

    /**
     * 标题。
     */
    private String title;

    /**
     * 描述。
     */
    private String description;

    /**
     * 比赛类型。
     */
    private String contestType;

    /**
     * 状态。
     */
    private String status;

    /**
     * 开始时间。
     */
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    private LocalDateTime endTime;

    /**
     * 持续时间（分钟）。
     */
    private Integer durationMinutes;

    /**
     * 封榜时长（分钟）。
     */
    private Integer freezeMinutes;

    /**
     * 计分规则。
     */
    private String scoringRule;

    /**
     * 罚时（分钟）。
     */
    private Integer penaltyTime;

    /**
     * 最大参赛人数。
     */
    private Integer maxParticipants;

    /**
     * 最大队伍人数。
     */
    private Integer maxTeamSize;

    /**
     * 最小队伍人数。
     */
    private Integer minTeamSize;

    /**
     * 是否公开。
     */
    private Boolean isPublic;

    /**
     * 在线判题平台。
     */
    private String ojPlatform;

    /**
     * 已报名人数。
     */
    private Integer registeredCount;

    /**
     * 队伍数量。
     */
    private Integer teamCount;

    /**
     * 题目数量。
     */
    private Integer problemCount;

    /**
     * 是否已报名。
     */
    private Boolean registered;

    /**
     * 当前用户是否为创建者。
     */
    private Boolean isCreator;

    /**
     * 草稿题目数据。
     */
    private String draftProblems;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    // ==================== Getter / Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }

    public Integer getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(Integer registeredCount) {
        this.registeredCount = registeredCount;
    }

    public Integer getTeamCount() {
        return teamCount;
    }

    public void setTeamCount(Integer teamCount) {
        this.teamCount = teamCount;
    }

    public Integer getProblemCount() {
        return problemCount;
    }

    public void setProblemCount(Integer problemCount) {
        this.problemCount = problemCount;
    }

    public Boolean getRegistered() {
        return registered;
    }

    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public Boolean getIsCreator() {
        return isCreator;
    }

    public void setIsCreator(Boolean isCreator) {
        this.isCreator = isCreator;
    }

    public String getDraftProblems() {
        return draftProblems;
    }

    public void setDraftProblems(String draftProblems) {
        this.draftProblems = draftProblems;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
