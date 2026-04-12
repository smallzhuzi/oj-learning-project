package com.ojplatform.config;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 洛谷 API 配置类
 * 绑定 application.yml 中 luogu 配置段
 * 注册独立的 OkHttpClient（带 CookieJar，统一管理所有 Cookie）
 */
@Configuration
@ConfigurationProperties(prefix = "luogu")
public class LuoguConfig {

    /** 洛谷基础 URL */
    private String baseUrl;

    /** 洛谷 _uid Cookie 值（通过环境变量 LUOGU_UID 注入） */
    private String uid;

    /** 洛谷 __client_id Cookie 值（通过环境变量 LUOGU_CLIENT_ID 注入） */
    private String clientId;

    /** 共享的 CookieJar 实例，可在 Service 中访问 */
    private LuoguCookieJar cookieJar;

    @Bean
    public OkHttpClient luoguHttpClient() {
        cookieJar = new LuoguCookieJar(this);
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .followRedirects(true)
                .cookieJar(cookieJar)
                .build();
    }

    @Bean
    public LuoguCookieJar luoguCookieJar() {
        return cookieJar;
    }

    /**
     * 洛谷 CookieJar：自动合并预配置 Cookie + 服务端 Set-Cookie
     * 预配置 Cookie（_uid、__client_id）始终随请求发送
     * 服务端返回的 Cookie（C3VK、会话 Cookie 等）自动存储和回传
     */
    public static class LuoguCookieJar implements CookieJar {
        private final LuoguConfig config;
        /** 存储服务端通过 Set-Cookie 返回的 cookie */
        private final Map<String, Map<String, Cookie>> serverCookies = new ConcurrentHashMap<>();

        public LuoguCookieJar(LuoguConfig config) {
            this.config = config;
        }

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            Map<String, Cookie> hostCookies = serverCookies.computeIfAbsent(url.host(), k -> new ConcurrentHashMap<>());
            for (Cookie cookie : cookies) {
                hostCookies.put(cookie.name(), cookie);
            }
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> result = new ArrayList<>();

            // 1. 预配置的认证 Cookie
            if (config.getUid() != null && !config.getUid().isBlank()) {
                result.add(buildCookie(url, "_uid", config.getUid()));
            }
            if (config.getClientId() != null && !config.getClientId().isBlank()) {
                result.add(buildCookie(url, "__client_id", config.getClientId()));
            }

            // 2. 服务端 Set-Cookie 返回的 cookie（C3VK、会话 cookie 等）
            Map<String, Cookie> hostCookies = serverCookies.get(url.host());
            if (hostCookies != null) {
                for (Cookie cookie : hostCookies.values()) {
                    // 不重复添加预配置的 cookie
                    if (!"_uid".equals(cookie.name()) && !"__client_id".equals(cookie.name())) {
                        result.add(cookie);
                    }
                }
            }

            return result;
        }

        /** 手动注入一个 Cookie（如从 JS 脚本中提取的 C3VK） */
        public void addCookie(String host, String name, String value) {
            HttpUrl url = HttpUrl.parse("https://" + host);
            if (url == null) return;
            Map<String, Cookie> hostCookies = serverCookies.computeIfAbsent(host, k -> new ConcurrentHashMap<>());
            hostCookies.put(name, buildCookie(url, name, value));
        }

        private static Cookie buildCookie(HttpUrl url, String name, String value) {
            return new Cookie.Builder()
                    .domain(url.host())
                    .path("/")
                    .name(name)
                    .value(value)
                    .build();
        }
    }

    // ==================== Getter / Setter ====================

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
}
