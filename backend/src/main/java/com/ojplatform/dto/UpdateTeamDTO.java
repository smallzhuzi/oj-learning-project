package com.ojplatform.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 更新比赛队伍数据传输对象。
 */
public class UpdateTeamDTO {

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 队伍名称。
     */
    @NotBlank(message = "队伍名称不能为空")
    private String teamName;

    /**
     * 描述。
     */
    private String description;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
}
