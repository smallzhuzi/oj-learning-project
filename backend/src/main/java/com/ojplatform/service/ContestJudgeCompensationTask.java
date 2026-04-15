package com.ojplatform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ojplatform.config.JudgeQueueProperties;
import com.ojplatform.entity.ContestSubmission;
import com.ojplatform.mapper.ContestSubmissionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 比赛判题补偿定时任务。
 */
@Component
public class ContestJudgeCompensationTask {

    private static final Logger log = LoggerFactory.getLogger(ContestJudgeCompensationTask.class);
    private static final String SUBMIT_REPAIR_PREFIX = "judge:repair:submit:";
    private static final String POLL_REPAIR_PREFIX = "judge:repair:poll:";

    @Autowired
    private ContestSubmissionMapper contestSubmissionMapper;

    @Autowired
    private ContestJudgeQueueService contestJudgeQueueService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JudgeQueueProperties judgeQueueProperties;

    @Scheduled(fixedDelayString = "${judge.queue.compensation-fixed-delay-ms:60000}")
    public void repairPendingJudgeTasks() {
        repairSubmitDispatch();
        repairPollDispatch();
    }

    private void repairSubmitDispatch() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(judgeQueueProperties.getSubmitCompensationDelaySeconds());
        List<ContestSubmission> submissions = contestSubmissionMapper.selectList(
                new LambdaQueryWrapper<ContestSubmission>()
                        .eq(ContestSubmission::getStatus, "Pending")
                        .isNull(ContestSubmission::getRemoteSubmissionId)
                        .lt(ContestSubmission::getSubmittedAt, threshold)
                        .orderByAsc(ContestSubmission::getSubmittedAt)
                        .last("limit " + judgeQueueProperties.getCompensationBatchSize())
        );

        for (ContestSubmission submission : submissions) {
            if (!tryThrottle(SUBMIT_REPAIR_PREFIX + submission.getId())) {
                continue;
            }
            try {
                contestJudgeQueueService.enqueueSubmit(submission.getId());
                log.info("Requeued missing submit task. submissionId={}", submission.getId());
            } catch (RuntimeException e) {
                log.warn("Failed to requeue submit task. submissionId={}, reason={}", submission.getId(), e.getMessage());
            }
        }
    }

    private void repairPollDispatch() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(judgeQueueProperties.getPollCompensationDelaySeconds());
        List<ContestSubmission> submissions = contestSubmissionMapper.selectList(
                new LambdaQueryWrapper<ContestSubmission>()
                        .eq(ContestSubmission::getStatus, "Pending")
                        .isNotNull(ContestSubmission::getRemoteSubmissionId)
                        .lt(ContestSubmission::getSubmittedAt, threshold)
                        .orderByAsc(ContestSubmission::getSubmittedAt)
                        .last("limit " + judgeQueueProperties.getCompensationBatchSize())
        );

        for (ContestSubmission submission : submissions) {
            if (!tryThrottle(POLL_REPAIR_PREFIX + submission.getId())) {
                continue;
            }
            try {
                contestJudgeQueueService.schedulePoll(submission.getId(), 1);
                log.info("Requeued missing poll task. submissionId={}", submission.getId());
            } catch (RuntimeException e) {
                log.warn("Failed to requeue poll task. submissionId={}, reason={}", submission.getId(), e.getMessage());
            }
        }
    }

    private boolean tryThrottle(String key) {
        Boolean allowed = stringRedisTemplate.opsForValue().setIfAbsent(
                key,
                "1",
                Duration.ofSeconds(judgeQueueProperties.getCompensationThrottleSeconds())
        );
        return Boolean.TRUE.equals(allowed);
    }
}
