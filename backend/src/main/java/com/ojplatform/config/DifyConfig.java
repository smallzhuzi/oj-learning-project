package com.ojplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Dify配置类。
 */
@Configuration
@ConfigurationProperties(prefix = "dify")
public class DifyConfig {

    /** Dify API 基础 URL（如 http://localhost/v1） */
    private String baseUrl;

    /** Dify API Key（通过环境变量 DIFY_API_KEY 注入） */
    private String apiKey;

    /** 智能组题应用的 API Key（通过环境变量 DIFY_SMART_GENERATE_KEY 注入） */
    private String smartGenerateKey;

    /** 赛后分析应用的 API Key（通过环境变量 DIFY_CONTEST_ANALYSIS_KEY 注入） */
    private String contestAnalysisKey;

    // ==================== Getter / Setter ====================

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSmartGenerateKey() {
        return smartGenerateKey;
    }

    public void setSmartGenerateKey(String smartGenerateKey) {
        this.smartGenerateKey = smartGenerateKey;
    }

    public String getContestAnalysisKey() {
        return contestAnalysisKey;
    }

    public void setContestAnalysisKey(String contestAnalysisKey) {
        this.contestAnalysisKey = contestAnalysisKey;
    }
}
