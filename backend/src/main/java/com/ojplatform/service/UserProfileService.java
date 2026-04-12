package com.ojplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ojplatform.dto.UpdateUserProfileDTO;
import com.ojplatform.entity.UserProfile;

/**
 * 用户画像服务接口
 */
public interface UserProfileService extends IService<UserProfile> {

    /**
     * 获取用户画像（不存在则自动创建默认画像）
     */
    UserProfile getOrCreateProfile(Long userId);

    /**
     * 更新用户自评水平和目标
     */
    UserProfile updateSelfAssessment(UpdateUserProfileDTO dto);

    /**
     * 根据用户做题数据自动分析更新画像
     * 统计 Easy/Medium/Hard 解题数、通过率、擅长/薄弱标签
     */
    UserProfile analyzeProfile(Long userId);
}
