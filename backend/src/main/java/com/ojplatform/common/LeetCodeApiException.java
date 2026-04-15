package com.ojplatform.common;

/**
 * 力扣接口异常类型。
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
