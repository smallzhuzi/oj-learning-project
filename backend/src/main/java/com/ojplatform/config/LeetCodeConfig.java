package com.ojplatform.config;

import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * LeetCode API 配置类
 * 绑定 application.yml 中 leetcode 配置段，注册全局 OkHttpClient
 */
@Configuration
@ConfigurationProperties(prefix = "leetcode")
public class LeetCodeConfig {

    /** LeetCode 基础 URL（如 https://leetcode.cn） */
    private String baseUrl;

    /** LeetCode 登录会话 Cookie（通过环境变量 LEETCODE_SESSION 注入） */
    private String session;

    /** CSRF Token（通过环境变量 LEETCODE_CSRF_TOKEN 注入） */
    private String csrfToken;

    /**
     * 全局共享的 OkHttpClient 实例
     * 线程安全，复用连接池，避免重复创建
     */
    @Bean
    public OkHttpClient leetCodeHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .followRedirects(true)
                .build();
    }

    // ==================== Getter / Setter ====================

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getCsrfToken() {
        return csrfToken;
    }

    public void setCsrfToken(String csrfToken) {
        this.csrfToken = csrfToken;
    }
}
