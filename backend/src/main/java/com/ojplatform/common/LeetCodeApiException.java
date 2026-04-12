package com.ojplatform.common;

/**
 * LeetCode API 调用专用异常
 * 继承通用 OjApiException，保持向后兼容
 */
public class LeetCodeApiException extends OjApiException {

    public LeetCodeApiException(String message) {
        super(message, "leetcode");
    }

    public LeetCodeApiException(String message, int httpStatus) {
        super(message, "leetcode", httpStatus);
    }

    public LeetCodeApiException(String message, Throwable cause) {
        super(message, "leetcode", cause);
    }
}
