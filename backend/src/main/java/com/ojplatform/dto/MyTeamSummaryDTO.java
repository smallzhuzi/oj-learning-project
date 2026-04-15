package com.ojplatform.dto;

import java.time.LocalDateTime;

/**
 * 我的队伍汇总数据传输对象。
 */
public class MyTeamSummaryDTO {

    /**
     * 比赛ID。
     */
    private Long contestId;
    /**
     * 比赛标题。
     */
    private String contestTitle;
    /**
     * 比赛状态。
     */
    private String contestStatus;
    /**
     * 队伍ID。
     */
    private Long teamId;
    /**
     * 队伍名称。
     */
    private String teamName;
    /**
     * 队长名称。
     */
    private String captainName;
    /**
     * 是否为队长。
     */
    private Boolean captain;
    /**
     * 是否已报名。
     */
    private Boolean registered;
    /**
     * 成员数量。
     */
    private Integer memberCount;
    /**
     * 最大队伍人数。
     */
    private Integer maxTeamSize;
    /**
     * 开始时间。
     */
    private LocalDateTime startTime;

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    public String getContestTitle() {
        return contestTitle;
    }

    public void setContestTitle(String contestTitle) {
        this.contestTitle = contestTitle;
    }

    public String getContestStatus() {
        return contestStatus;
    }

    public void setContestStatus(String contestStatus) {
        this.contestStatus = contestStatus;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getCaptainName() {
        return captainName;
    }

    public void setCaptainName(String captainName) {
        this.captainName = captainName;
    }

    public Boolean getCaptain() {
        return captain;
    }

    public void setCaptain(Boolean captain) {
        this.captain = captain;
    }

    public Boolean getRegistered() {
        return registered;
    }

    public void setRegistered(Boolean registered) {
        this.registered = registered;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public Integer getMaxTeamSize() {
        return maxTeamSize;
    }

    public void setMaxTeamSize(Integer maxTeamSize) {
        this.maxTeamSize = maxTeamSize;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
}
