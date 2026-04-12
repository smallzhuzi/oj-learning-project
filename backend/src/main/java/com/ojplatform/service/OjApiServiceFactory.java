package com.ojplatform.service;

import com.ojplatform.common.OjApiException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OJ 平台 API 服务工厂
 * 通过 Spring 自动注入所有 OjApiService 实现，按平台标识路由
 */
@Component
public class OjApiServiceFactory {

    private final Map<String, OjApiService> serviceMap;

    public OjApiServiceFactory(List<OjApiService> services) {
        this.serviceMap = services.stream()
                .collect(Collectors.toMap(OjApiService::getPlatform, s -> s));
    }

    /**
     * 根据平台标识获取对应的 API 服务
     * @param platform 平台标识（leetcode / luogu）
     * @return 对应的 OjApiService 实现
     * @throws OjApiException 如果平台不支持
     */
    public OjApiService getService(String platform) {
        OjApiService service = serviceMap.get(platform);
        if (service == null) {
            throw new OjApiException("不支持的 OJ 平台: " + platform, platform);
        }
        return service;
    }

    /**
     * 检查是否支持指定平台
     */
    public boolean supports(String platform) {
        return serviceMap.containsKey(platform);
    }
}
