package com.ojplatform.common;

/**
 * 判题接口异常类型。
 */
public class OjApiException extends RuntimeException {

    /** OJ 平台标识（leetcode / luogu） */
    private final String platform;

    /** HTTP 状态码（非 HTTP 错误时为 500） */
    private final int httpStatus;

    public OjApiException(String message) {
        super(message);
        this.platform = "unknown";
        this.httpStatus = 500;
    }

    public OjApiException(String message, String platform) {
        super(message);
        this.platform = platform;
        this.httpStatus = 500;
    }

    public OjApiException(String message, String platform, int httpStatus) {
        super(message);
        this.platform = platform;
        this.httpStatus = httpStatus;
    }

    public OjApiException(String message, String platform, Throwable cause) {
        super(message, cause);
        this.platform = platform;
        this.httpStatus = 500;
    }

    public String getPlatform() {
        return platform;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
