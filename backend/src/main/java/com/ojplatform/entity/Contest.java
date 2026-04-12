package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 比赛表实体类
 * 对应数据库表：contests
 * 存储比赛的基本信息、规则配置和生命周期状态
 */
@TableName("contests")
public class Contest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 比赛唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建者用户 ID */
    private Long creatorId;

    /** 比赛标题 */
    private String title;

    /** 比赛说明 */
    private String description;

    /** 比赛类型：individual / team */
    private String contestType;

    /** 状态：draft / registering / running / frozen / ended / archived */
    private String status;

    /** 关联题单 ID */
    private Long problemSetId;

    /** 比赛开始时间 */
    private LocalDateTime startTime;

    /** 比赛结束时间 */
    private LocalDateTime endTime;

    /** 比赛时长（分钟） */
    private Integer durationMinutes;

    /** 封榜时间（比赛结束前 N 分钟，0 = 不封榜） */
    private Integer freezeMinutes;

    /** 最大参赛人数（0 = 不限） */
    private Integer maxParticipants;

    /** 组队赛最大队伍人数 */
    private Integer maxTeamSize;

    /** 计分规则：acm / oi / cf */
    private String scoringRule;

    /** ACM 罚时：每次错误提交罚 N 分钟 */
    private Integer penaltyTime;

    /** 允许的编程语言 JSON 数组 */
    @JsonRawValue
    private String allowLanguage;

    /** 是否公开 */
    private Boolean isPublic;

    /** 私有比赛密码 */
    private String password;

    /** OJ 平台标识 */
    private String ojPlatform;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
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
