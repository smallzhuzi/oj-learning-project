package com.ojplatform.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 转让队长数据传输对象。
 */
public class TransferCaptainDTO {

    /**
     * 用户ID。
     */
    private Long userId;

    /**
     * 目标用户ID。
     */
    @NotNull(message = "目标队长不能为空")
    private Long targetUserId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }
}
