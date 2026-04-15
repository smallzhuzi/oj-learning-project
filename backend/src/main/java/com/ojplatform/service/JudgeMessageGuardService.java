package com.ojplatform.service;

import com.ojplatform.dto.JudgeTaskMessage;

/**
 * 判题消息去重相关业务接口。
 */
public interface JudgeMessageGuardService {

    boolean tryAcquire(JudgeTaskMessage message);
}
