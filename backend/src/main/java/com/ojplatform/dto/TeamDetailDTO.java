package com.ojplatform.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 队伍详情数据传输对象。
 */
public class TeamDetailDTO {

    /**
     * 唯一标识。
     */
    private Long id;
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
    private List<MemberInfo> members;

    /**
     * 比赛列表。
     */
    private List<ContestRecord> contests;

    /**
     * 待处理申请数量。
     */
    private int pendingRequestCount;

    // ==================== 内部类 ====================

    /**
     * 成员信息数据传输对象。
     */
    public static class MemberInfo {
        /**
         * 用户ID。
         */
        private Long userId;
        /**
         * 用户名。
         */
        private String username;
        /**
         * 角色。
         */
        private String role;
        /**
         * 加入时间。
         */
        private LocalDateTime joinedAt;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public LocalDateTime getJoinedAt() { return joinedAt; }
        public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    }

    /**
     * 比赛记录数据传输对象。
     */
    public static class ContestRecord {
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
         * 报名状态。
         */
        private String registrationStatus;
        /**
         * 开始时间。
         */
        private LocalDateTime startTime;

        public Long getContestId() { return contestId; }
        public void setContestId(Long contestId) { this.contestId = contestId; }
        public String getContestTitle() { return contestTitle; }
        public void setContestTitle(String contestTitle) { this.contestTitle = contestTitle; }
        public String getContestStatus() { return contestStatus; }
        public void setContestStatus(String contestStatus) { this.contestStatus = contestStatus; }
        public String getRegistrationStatus() { return registrationStatus; }
        public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    }

    // ==================== Getter / Setter ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCaptainId() { return captainId; }
    public void setCaptainId(Long captainId) { this.captainId = captainId; }

    public String getCaptainName() { return captainName; }
    public void setCaptainName(String captainName) { this.captainName = captainName; }

    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isCaptain() { return captain; }
    public void setCaptain(boolean captain) { this.captain = captain; }

    public String getMyRole() { return myRole; }
    public void setMyRole(String myRole) { this.myRole = myRole; }

    public List<MemberInfo> getMembers() { return members; }
    public void setMembers(List<MemberInfo> members) { this.members = members; }

    public List<ContestRecord> getContests() { return contests; }
    public void setContests(List<ContestRecord> contests) { this.contests = contests; }

    public int getPendingRequestCount() { return pendingRequestCount; }
    public void setPendingRequestCount(int pendingRequestCount) { this.pendingRequestCount = pendingRequestCount; }
}
