package com.ojplatform.service.impl;

import com.ojplatform.config.JudgeQueueProperties;
import com.ojplatform.dto.JudgeTaskMessage;
import com.ojplatform.service.JudgeMessageGuardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 判题消息去重相关业务实现。
 */
@Service
public class JudgeMessageGuardServiceImpl implements JudgeMessageGuardService {

    private static final String MESSAGE_KEY_PREFIX = "judge:message:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JudgeQueueProperties judgeQueueProperties;

    @Override
    public boolean tryAcquire(JudgeTaskMessage message) {
        if (message == null || message.getMessageId() == null || message.getMessageId().isBlank()) {
            throw new IllegalArgumentException("Judge task message missing messageId");
        }

        Boolean firstSeen = stringRedisTemplate.opsForValue().setIfAbsent(
                MESSAGE_KEY_PREFIX + message.getMessageId(),
                "1",
                Duration.ofMillis(judgeQueueProperties.getMessageDedupTtlMs())
        );
        return Boolean.TRUE.equals(firstSeen);
    }
}
