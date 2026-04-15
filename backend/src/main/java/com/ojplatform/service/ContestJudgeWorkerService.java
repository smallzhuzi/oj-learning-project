package com.ojplatform.service;

import com.ojplatform.dto.JudgeTaskMessage;

/**
 * 比赛判题执行相关业务接口。
 */
public interface ContestJudgeWorkerService {

    void processSubmit(JudgeTaskMessage message);

    void processPoll(JudgeTaskMessage message);
}
