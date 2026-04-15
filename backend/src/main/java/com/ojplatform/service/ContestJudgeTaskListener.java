package com.ojplatform.service;

import com.ojplatform.dto.JudgeTaskMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 比赛判题Task消息监听器。
 */
@Component
public class ContestJudgeTaskListener {

    @Autowired
    private ContestJudgeWorkerService contestJudgeWorkerService;

    @Autowired
    private JudgeMessageGuardService judgeMessageGuardService;

    @RabbitListener(queues = "${judge.queue.contest-submit-queue}")
    public void handleSubmit(JudgeTaskMessage message) {
        if (!judgeMessageGuardService.tryAcquire(message)) {
            return;
        }
        contestJudgeWorkerService.processSubmit(message);
    }

    @RabbitListener(queues = "${judge.queue.contest-poll-queue}")
    public void handlePoll(JudgeTaskMessage message) {
        if (!judgeMessageGuardService.tryAcquire(message)) {
            return;
        }
        contestJudgeWorkerService.processPoll(message);
    }
}
