package com.ojplatform.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 比赛队伍详情数据传输对象。
 */
public class ContestTeamDetailDTO {

    /**
     * 唯一标识。
     */
    private Long id;
    /**
     * 比赛ID。
     */
    private Long contestId;
    /**
     * 队伍名称。
     */
    private String teamName;
    /**
     * 描述。
     */
    private String description;
    /**
     * 队长用户ID。
     */
    private Long captainId;
    /**
     * 队长名称。
     */
    private String captainName;
    /**
     * 成员数量。
     */
    private Integer memberCount;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
    /**
     * 是否为队长。
     */
    private boolean captain;
    /**
     * 我的角色。
     */
    private String myRole;
    /**
     * 成员列表。
     */
    private List<ContestTeamMemberDTO> members;

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

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCaptainId() {
        return captainId;
    }

    public void setCaptainId(Long captainId) {
        this.captainId = captainId;
    }

    public String getCaptainName() {
        return captainName;
    }

    public void setCaptainName(String captainName) {
        this.captainName = captainName;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCaptain() {
        return captain;
    }

    public void setCaptain(boolean captain) {
        this.captain = captain;
    }

    public String getMyRole() {
        return myRole;
    }

    public void setMyRole(String myRole) {
        this.myRole = myRole;
    }

    public List<ContestTeamMemberDTO> getMembers() {
        return members;
    }

    public void setMembers(List<ContestTeamMemberDTO> members) {
        this.members = members;
    }
}
