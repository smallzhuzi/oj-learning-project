package com.ojplatform.service;

/**
 * 比赛判题队列相关业务接口。
 */
public interface ContestJudgeQueueService {

    void enqueueSubmit(Long submissionId);

    void schedulePoll(Long submissionId, int attempt);
}
