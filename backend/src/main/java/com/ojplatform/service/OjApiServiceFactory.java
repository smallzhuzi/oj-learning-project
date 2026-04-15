package com.ojplatform.service;

import com.ojplatform.common.OjApiException;
import com.ojplatform.dto.OjJudgeResult;
import com.ojplatform.dto.OjProblemDetail;
import com.ojplatform.entity.Problem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 判题接口ServiceFactory核心组件。
 */
@Component
public class OjApiServiceFactory {

    private final Map<String, OjApiService> serviceMap;
    private final OjExecutionService ojExecutionService;

    public OjApiServiceFactory(List<OjApiService> services, OjExecutionService ojExecutionService) {
        this.serviceMap = services.stream().collect(Collectors.toMap(OjApiService::getPlatform, service -> service));
        this.ojExecutionService = ojExecutionService;
    }

    public OjApiService getService(String platform) {
        OjApiService delegate = serviceMap.get(platform);
        if (delegate == null) {
            throw new OjApiException("不支持的 OJ 平台: " + platform, platform);
        }

        return new OjApiService() {
            @Override
            public String getPlatform() {
                return delegate.getPlatform();
            }

            @Override
            public List<Problem> fetchProblemList(int skip, int limit, String keyword) {
                return delegate.fetchProblemList(skip, limit, keyword);
            }

            @Override
            public OjProblemDetail fetchProblemDetail(String slug) {
                return delegate.fetchProblemDetail(slug);
            }

            @Override
            public String submitCode(String slug, String questionId, String lang, String code) {
                return ojExecutionService.submitCodeWithRetry(delegate, slug, questionId, lang, code);
            }

            @Override
            public OjJudgeResult checkResult(String remoteSubmissionId) {
                OjJudgeResult result = ojExecutionService.checkResultWithRetry(delegate, remoteSubmissionId);
                if (result == null) {
                    OjJudgeResult pending = new OjJudgeResult();
                    pending.setFinished(false);
                    return pending;
                }
                return result;
            }

            @Override
            public String mapLanguage(String commonLangSlug) {
                return delegate.mapLanguage(commonLangSlug);
            }
        };
    }

    public boolean supports(String platform) {
        return serviceMap.containsKey(platform);
    }
}
