package com.ojplatform.service;

import com.ojplatform.dto.UserOjConfigDTO;
import com.ojplatform.entity.UserOjConfig;
import java.util.List;

/**
 * 用户 OJ 平台配置服务接口
 */
public interface UserOjConfigService {

    /** 获取用户所有 OJ 配置列表 */
    List<UserOjConfig> getUserConfigs(Long userId);

    /** 获取用户某平台的配置 */
    UserOjConfig getConfig(Long userId, String ojPlatform);

    /** 保存或更新配置（upsert） */
    void saveOrUpdateConfig(Long userId, UserOjConfigDTO dto);
}
