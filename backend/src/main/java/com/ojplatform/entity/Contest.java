package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 比赛实体类。
 */
@TableName("contests")
public class Contest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建者用户ID。
     */
    private Long creatorId;

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
     * 题单ID。
     */
    private Long problemSetId;

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
     * 计分规则。
     */
    private String scoringRule;

    /**
     * 罚时（分钟）。
     */
    private Integer penaltyTime;

    /**
     * 允许的编程语言。
     */
    @JsonRawValue
    private String allowLanguage;

    /**
     * 是否公开。
     */
    private Boolean isPublic;

    /**
     * 密码。
     */
    private String password;

    /**
     * 在线判题平台。
     */
    private String ojPlatform;

    /**
     * 草稿题目数据。
     */
    private String draftProblems;

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ==================== Getter / Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
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

    public Long getProblemSetId() {
        return problemSetId;
    }

    public void setProblemSetId(Long problemSetId) {
        this.problemSetId = problemSetId;
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

    @JsonRawValue
    public String getAllowLanguage() {
        return allowLanguage;
    }

    public void setAllowLanguage(String allowLanguage) {
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
