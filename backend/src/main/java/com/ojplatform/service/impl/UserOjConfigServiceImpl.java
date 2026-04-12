package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ojplatform.dto.UserOjConfigDTO;
import com.ojplatform.entity.UserOjConfig;
import com.ojplatform.mapper.UserOjConfigMapper;
import com.ojplatform.service.UserOjConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户 OJ 平台配置服务实现
 */
@Service
public class UserOjConfigServiceImpl extends ServiceImpl<UserOjConfigMapper, UserOjConfig>
        implements UserOjConfigService {

    @Override
    public List<UserOjConfig> getUserConfigs(Long userId) {
        return list(new LambdaQueryWrapper<UserOjConfig>()
                .eq(UserOjConfig::getUserId, userId));
    }

    @Override
    public UserOjConfig getConfig(Long userId, String ojPlatform) {
        return getOne(new LambdaQueryWrapper<UserOjConfig>()
                .eq(UserOjConfig::getUserId, userId)
                .eq(UserOjConfig::getOjPlatform, ojPlatform));
    }

    @Override
    public void saveOrUpdateConfig(Long userId, UserOjConfigDTO dto) {
        UserOjConfig existing = getConfig(userId, dto.getOjPlatform());
        if (existing != null) {
            existing.setCookieValue(dto.getCookieValue());
            existing.setCsrfToken(dto.getCsrfToken());
            existing.setExtraConfig(dto.getExtraConfig());
            updateById(existing);
        } else {
            UserOjConfig config = new UserOjConfig();
            config.setUserId(userId);
            config.setOjPlatform(dto.getOjPlatform());
            config.setCookieValue(dto.getCookieValue());
            config.setCsrfToken(dto.getCsrfToken());
            config.setExtraConfig(dto.getExtraConfig());
            save(config);
        }
    }
}
