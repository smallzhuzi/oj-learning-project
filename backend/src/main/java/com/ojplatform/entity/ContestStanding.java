package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonRawValue;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 榜单快照表实体类
 * 对应数据库表：contest_standings
 * 存储比赛榜单数据，支持封榜机制和历史回溯
 */
@TableName("contest_standings")
public class ContestStanding implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 榜单记录唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 比赛 ID */
    private Long contestId;

    /** 用户 ID（个人赛时使用） */
    private Long userId;

    /** 队伍 ID（组队赛时使用） */
    private Long teamId;

    /** 排名 */
    @TableField("`rank`")
    private Integer rank;

    /** 解题数 */
    private Integer solvedCount;

    /** 总分（OI 赛制） */
    private Integer totalScore;

    /** ACM 罚时（秒） */
    private Long totalPenalty;

    /** 每题详情 JSON */
    @JsonRawValue
    private String problemDetails;

    /** 是否为封榜时刻的快照 */
    private Boolean isFrozen;

    /** 快照时间 */
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
