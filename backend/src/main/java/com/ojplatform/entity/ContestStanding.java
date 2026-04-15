package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 比赛榜单实体类。
 */
@TableName("contest_standings")
public class ContestStanding implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 比赛ID。
     */
    private Long contestId;

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 队伍ID。
     */
    private Long teamId;

    /**
     * 排名。
     */
    @TableField("`rank`")
    private Integer rank;

    /**
     * 通过题数。
     */
    private Integer solvedCount;

    /**
     * 总分。
     */
    private Integer totalScore;

    /**
     * 总罚时。
     */
    private Long totalPenalty;

    /**
     * 题目详情数据。
     */
    @JsonRawValue
    private String problemDetails;

    /**
     * 是否为封榜快照。
     */
    private Boolean isFrozen;

    /**
     * 快照时间。
     */
    private LocalDateTime snapshotTime;

    // ==================== Getter / Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getSolvedCount() {
        return solvedCount;
    }

    public void setSolvedCount(Integer solvedCount) {
        this.solvedCount = solvedCount;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Long getTotalPenalty() {
        return totalPenalty;
    }

    public void setTotalPenalty(Long totalPenalty) {
        this.totalPenalty = totalPenalty;
    }

    @JsonRawValue
    public String getProblemDetails() {
        return problemDetails;
    }

    public void setProblemDetails(String problemDetails) {
        this.problemDetails = problemDetails;
    }

    public Boolean getIsFrozen() {
        return isFrozen;
    }

    public void setIsFrozen(Boolean isFrozen) {
        this.isFrozen = isFrozen;
    }

    public LocalDateTime getSnapshotTime() {
        return snapshotTime;
    }

    public void setSnapshotTime(LocalDateTime snapshotTime) {
        this.snapshotTime = snapshotTime;
    }
}
