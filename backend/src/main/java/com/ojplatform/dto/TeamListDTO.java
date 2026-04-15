package com.ojplatform.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 队伍列表数据传输对象。
 */
public class TeamListDTO {

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
     * 成员名称列表。
     */
    private List<String> memberNames;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

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

    public List<String> getMemberNames() { return memberNames; }
    public void setMemberNames(List<String> memberNames) { this.memberNames = memberNames; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
