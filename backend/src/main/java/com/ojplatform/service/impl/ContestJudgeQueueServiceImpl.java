package com.ojplatform.service.impl;

import com.ojplatform.config.JudgeQueueProperties;
import com.ojplatform.dto.JudgeTaskMessage;
import com.ojplatform.service.ContestJudgeQueueService;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 比赛判题队列相关业务实现。
 */
@Service
public class ContestJudgeQueueServiceImpl implements ContestJudgeQueueService {

    private static final String TASK_SUBMIT = "submit";
    private static final String TASK_POLL = "poll";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private JudgeQueueProperties judgeQueueProperties;

    @Override
    public void enqueueSubmit(Long submissionId) {
        rabbitTemplate.convertAndSend(
                judgeQueueProperties.getExchange(),
                judgeQueueProperties.getContestSubmitRoutingKey(),
                buildMessage(submissionId, TASK_SUBMIT, 1)
        );
    }

    @Override
    public void schedulePoll(Long submissionId, int attempt) {
        long delayMs = attempt <= 1
                ? judgeQueueProperties.getInitialPollDelayMs()
                : Math.min(
                        judgeQueueProperties.getMaxPollDelayMs(),
                        judgeQueueProperties.getPollDelayMs() * Math.min(attempt - 1L, 5L)
                );

        MessagePostProcessor delayProcessor = message -> {
            message.getMessageProperties().setExpiration(String.valueOf(delayMs));
            return message;
        };

        rabbitTemplate.convertAndSend(
                judgeQueueProperties.getExchange(),
                judgeQueueProperties.getContestPollDelayRoutingKey(),
                buildMessage(submissionId, TASK_POLL, attempt),
                delayProcessor
        );
    }

    private JudgeTaskMessage buildMessage(Long submissionId, String taskType, int attempt) {
        return new JudgeTaskMessage(
                UUID.randomUUID().toString(),
                submissionId,
                taskType,
                attempt,
                LocalDateTime.now()
        );
    }
}
