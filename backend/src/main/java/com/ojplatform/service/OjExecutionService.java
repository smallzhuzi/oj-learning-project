package com.ojplatform.service;

import com.ojplatform.common.OjApiException;
import com.ojplatform.dto.OjJudgeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 判题执行相关业务接口。
 */
@Service
public class OjExecutionService {

    private static final Logger log = LoggerFactory.getLogger(OjExecutionService.class);
    private static final Set<Integer> RETRYABLE_HTTP_STATUS = Set.of(429, 500, 502, 503, 504);

    public String submitCodeWithRetry(OjApiService apiService, String slug, String questionId, String lang, String code) {
        return executeWithRetry(
                "submit",
                apiService.getPlatform(),
                () -> apiService.submitCode(slug, questionId, lang, code),
                3
        );
    }

    public OjJudgeResult checkResultWithRetry(OjApiService apiService, String remoteSubmissionId) {
        try {
            return executeWithRetry(
                    "poll",
                    apiService.getPlatform(),
                    () -> apiService.checkResult(remoteSubmissionId),
                    2
            );
        } catch (OjApiException e) {
            if (isRetryable(e)) {
                log.warn("Remote judge still unstable after retry, keep pending. platform={}, remoteSubmissionId={}, message={}",
                        apiService.getPlatform(), remoteSubmissionId, e.getMessage());
                return null;
            }
            throw e;
        }
    }

    private <T> T executeWithRetry(String action, String platform, SupplierWithException<T> supplier, int maxAttempts) {
        OjApiException lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return supplier.get();
            } catch (OjApiException e) {
                lastException = e;
                if (!isRetryable(e) || attempt == maxAttempts) {
                    throw e;
                }
                long sleepMs = 300L * attempt;
                log.warn("Remote judge {} failed, retrying. platform={}, attempt={}/{}, status={}, message={}",
                        action, platform, attempt, maxAttempts, e.getHttpStatus(), e.getMessage());
                sleepQuietly(sleepMs);
            }
        }
        throw lastException != null ? lastException : new OjApiException("远程判题调用失败", platform);
    }

    private boolean isRetryable(OjApiException e) {
        return RETRYABLE_HTTP_STATUS.contains(e.getHttpStatus())
                || e.getCause() != null
                || (e.getMessage() != null && (
                e.getMessage().contains("网络")
                        || e.getMessage().contains("超时")
                        || e.getMessage().contains("频率")
                        || e.getMessage().contains("连接")
        ));
    }

    private void sleepQuietly(long sleepMs) {
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("重试等待被中断", e);
        }
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get();
    }
}
