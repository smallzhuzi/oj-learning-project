package com.ojplatform.service.impl;

import com.ojplatform.config.JudgeQueueProperties;
import com.ojplatform.dto.JudgeTaskMessage;
import com.ojplatform.dto.OjJudgeResult;
import com.ojplatform.dto.ProblemSetItemDetailDTO;
import com.ojplatform.entity.Contest;
import com.ojplatform.entity.ContestSubmission;
import com.ojplatform.entity.Problem;
import com.ojplatform.mapper.ContestMapper;
import com.ojplatform.mapper.ContestSubmissionMapper;
import com.ojplatform.service.ContestJudgeQueueService;
import com.ojplatform.service.ContestJudgeWorkerService;
import com.ojplatform.service.ContestStandingSnapshotService;
import com.ojplatform.service.OjApiService;
import com.ojplatform.service.OjApiServiceFactory;
import com.ojplatform.service.ProblemService;
import com.ojplatform.service.ProblemSetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 比赛判题执行相关业务实现。
 */
@Service
public class ContestJudgeWorkerServiceImpl implements ContestJudgeWorkerService {

    private static final Logger log = LoggerFactory.getLogger(ContestJudgeWorkerServiceImpl.class);

    @Autowired
    private ContestSubmissionMapper contestSubmissionMapper;

    @Autowired
    private ContestMapper contestMapper;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private ProblemSetService problemSetService;

    @Autowired
    private OjApiServiceFactory ojApiServiceFactory;

    @Autowired
    private ContestJudgeQueueService contestJudgeQueueService;

    @Autowired
    private ContestStandingSnapshotService contestStandingSnapshotService;

    @Autowired
    private JudgeQueueProperties judgeQueueProperties;

    /**
     * 消费提交任务，并把代码送入远程 OJ。
     */
    @Override
    public void processSubmit(JudgeTaskMessage message) {
        ContestSubmission submission = contestSubmissionMapper.selectById(message.getSubmissionId());
        if (submission == null) {
            return;
        }
        if (!"Pending".equals(submission.getStatus()) || submission.getRemoteSubmissionId() != null) {
            return;
        }

        Contest contest = contestMapper.selectById(submission.getContestId());
        Problem problem = problemService.getById(submission.getProblemId());
        if (contest == null || problem == null) {
            markSubmitFailed(submission, "contest or problem missing");
            return;
        }

        try {
            OjApiService apiService = ojApiServiceFactory.getService(contest.getOjPlatform());
            String platformLang = apiService.mapLanguage(submission.getLanguage());
            String remoteId = apiService.submitCode(
                    problem.getSlug(),
                    problem.getQuestionId(),
                    platformLang,
                    submission.getCode()
            );
            submission.setRemoteSubmissionId(remoteId);
            contestSubmissionMapper.updateById(submission);
            try {
                contestJudgeQueueService.schedulePoll(submission.getId(), 1);
            } catch (RuntimeException e) {
                markJudgeFailed(submission, "poll queue dispatch failed: " + e.getMessage());
                return;
            }
            log.info("Contest submission dispatched. contestId={}, submissionId={}, remoteId={}",
                    submission.getContestId(), submission.getId(), remoteId);
        } catch (RuntimeException e) {
            markSubmitFailed(submission, e.getMessage());
        }
    }

    /**
     * 消费轮询任务，并把判题终态同步到本地比赛提交记录。
     */
    @Override
    public void processPoll(JudgeTaskMessage message) {
        ContestSubmission submission = contestSubmissionMapper.selectById(message.getSubmissionId());
        if (submission == null) {
            return;
        }
        if (!"Pending".equals(submission.getStatus()) || submission.getRemoteSubmissionId() == null) {
            return;
        }

        Contest contest = contestMapper.selectById(submission.getContestId());
        if (contest == null) {
            markJudgeFailed(submission, "contest missing");
            return;
        }

        try {
            OjApiService apiService = ojApiServiceFactory.getService(contest.getOjPlatform());
            OjJudgeResult result = apiService.checkResult(submission.getRemoteSubmissionId());
            if (result.isFinished()) {
                applyJudgeResult(contest, submission, result);
                return;
            }

            if (message.getAttempt() >= judgeQueueProperties.getMaxPollAttempts()) {
                submission.setStatus("Judge Timeout");
                contestSubmissionMapper.updateById(submission);
                contestStandingSnapshotService.refreshStandingForSubmission(submission);
                log.warn("Contest judging timed out. contestId={}, submissionId={}",
                        submission.getContestId(), submission.getId());
                return;
            }

            contestJudgeQueueService.schedulePoll(submission.getId(), message.getAttempt() + 1);
        } catch (RuntimeException e) {
            markJudgeFailed(submission, e.getMessage());
        }
    }

    /**
     * 根据判题结果更新分数、状态和榜单快照。
     */
    private void applyJudgeResult(Contest contest, ContestSubmission submission, OjJudgeResult result) {
        submission.setStatus(result.getStatusMsg());
        submission.setRuntime(result.getRuntime());
        submission.setMemory(result.getMemory());
        submission.setTotalCorrect(result.getTotalCorrect());
        submission.setTotalTestcases(result.getTotalTestcases());

        if ("oi".equals(contest.getScoringRule()) && result.getTotalTestcases() != null && result.getTotalTestcases() > 0) {
            submission.setScore(resolveOiScore(contest, submission, result));
        } else if ("Accepted".equals(result.getStatusMsg())) {
            submission.setScore(100);
        } else {
            submission.setScore(0);
        }

        contestSubmissionMapper.updateById(submission);
        contestStandingSnapshotService.refreshStandingForSubmission(submission);
        log.info("Contest judging finished. contestId={}, submissionId={}, status={}",
                submission.getContestId(), submission.getId(), result.getStatusMsg());
    }

    private int resolveOiScore(Contest contest, ContestSubmission submission, OjJudgeResult result) {
        int fullScore = 100;
        if (contest.getProblemSetId() != null) {
            List<ProblemSetItemDetailDTO> items = problemSetService.getProblemSetItems(contest.getProblemSetId());
            for (ProblemSetItemDetailDTO item : items) {
                if (item.getProblemId().equals(submission.getProblemId())) {
                    fullScore = item.getScore();
                    break;
                }
            }
        }
        return (int) Math.round((double) result.getTotalCorrect() / result.getTotalTestcases() * fullScore);
    }

    private void markSubmitFailed(ContestSubmission submission, String reason) {
        submission.setStatus("Submit Failed");
        contestSubmissionMapper.updateById(submission);
        contestStandingSnapshotService.refreshStandingForSubmission(submission);
        log.warn("Contest submit dispatch failed. contestId={}, submissionId={}, reason={}",
                submission.getContestId(), submission.getId(), reason);
    }

    private void markJudgeFailed(ContestSubmission submission, String reason) {
        submission.setStatus("Judge Failed");
        contestSubmissionMapper.updateById(submission);
        contestStandingSnapshotService.refreshStandingForSubmission(submission);
        log.warn("Contest judging failed. contestId={}, submissionId={}, reason={}",
                submission.getContestId(), submission.getId(), reason);
    }
}
