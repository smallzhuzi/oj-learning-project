package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ojplatform.dto.OjJudgeResult;
import com.ojplatform.dto.ProblemStatusDTO;
import com.ojplatform.dto.SubmitCodeDTO;
import com.ojplatform.dto.UserStatsDTO;
import com.ojplatform.entity.Problem;
import com.ojplatform.entity.Submission;
import com.ojplatform.mapper.SubmissionMapper;
import com.ojplatform.service.OjApiService;
import com.ojplatform.service.OjApiServiceFactory;
import com.ojplatform.service.ProblemService;
import com.ojplatform.service.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 提交相关业务实现。
 */
@Service
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionServiceImpl.class);

    @Autowired
    private ProblemService problemService;

    @Autowired
    private OjApiServiceFactory ojApiServiceFactory;

    /**
     * 创建本地提交记录，并同步转发到远程 OJ。
     */
    @Override
    public Submission submitCode(SubmitCodeDTO dto) {
        String ojPlatform = dto.getOjPlatform();

        Problem problem = problemService.getBySlug(dto.getProblemSlug(), ojPlatform);
        if (problem == null) {
            throw new RuntimeException("题目不存在：" + dto.getProblemSlug());
        }

        Submission submission = new Submission();
        submission.setUserId(dto.getUserId());
        submission.setProblemId(problem.getId());
        submission.setSessionId(dto.getSessionId());
        submission.setLanguage(dto.getLanguage());
        submission.setCode(dto.getCode());
        submission.setStatus("Pending");
        baseMapper.insert(submission);

        OjApiService apiService = ojApiServiceFactory.getService(ojPlatform);
        String platformLang = apiService.mapLanguage(dto.getLanguage());
        try {
            String remoteId = apiService.submitCode(problem.getSlug(), problem.getQuestionId(), platformLang, dto.getCode());
            submission.setRemoteSubmissionId(remoteId);
            baseMapper.updateById(submission);
            log.info("代码提交成功：ojPlatform={}, problemSlug={}, remoteId={}", ojPlatform, dto.getProblemSlug(), remoteId);
        } catch (RuntimeException e) {
            submission.setStatus("Submit Failed");
            baseMapper.updateById(submission);
            throw new RuntimeException("远程判题服务暂时不可用，提交未进入判题队列，请稍后重试");
        }

        return submission;
    }

    /**
     * 拉取远程判题结果，并把终态回写到本地提交记录。
     */
    @Override
    public Submission pollResult(Long submissionId) {
        Submission submission = baseMapper.selectById(submissionId);
        if (submission == null) {
            throw new RuntimeException("提交记录不存在：" + submissionId);
        }

        if (submission.getRemoteSubmissionId() == null) {
            return submission;
        }

        if (!"Pending".equals(submission.getStatus())) {
            return submission;
        }

        Problem problem = problemService.getById(submission.getProblemId());
        OjApiService apiService = ojApiServiceFactory.getService(problem.getOjPlatform());
        OjJudgeResult result = apiService.checkResult(submission.getRemoteSubmissionId());

        if (result.isFinished()) {
            submission.setStatus(result.getStatusMsg());
            submission.setRuntime(result.getRuntime());
            submission.setMemory(result.getMemory());
            submission.setTotalCorrect(result.getTotalCorrect());
            submission.setTotalTestcases(result.getTotalTestcases());
            baseMapper.updateById(submission);
            log.info("判题完成：submissionId={}, status={}", submissionId, result.getStatusMsg());
        }

        return submission;
    }

    @Override
    public List<Submission> getSessionSubmissions(Long sessionId) {
        return baseMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getSessionId, sessionId)
                        .orderByAsc(Submission::getSubmittedAt)
        );
    }

    @Override
    public List<Submission> getUserProblemSubmissions(Long userId, String problemSlug, String ojPlatform) {
        Problem problem = problemService.getBySlug(problemSlug, ojPlatform);
        if (problem == null) {
            return List.of();
        }
        return baseMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getUserId, userId)
                        .eq(Submission::getProblemId, problem.getId())
                        .ne(Submission::getStatus, "Pending")
                        .orderByDesc(Submission::getSubmittedAt)
        );
    }

    @Override
    public Map<String, String> getUserStatusMap(Long userId) {
        List<ProblemStatusDTO> list = baseMapper.selectUserStatusMap(userId);
        return list.stream().collect(Collectors.toMap(
                ProblemStatusDTO::getSlug,
                ProblemStatusDTO::getStatus
        ));
    }

    /**
     * 汇总用户提交、通过率和活跃天数等统计信息。
     */
    @Override
    public UserStatsDTO getUserStats(Long userId) {
        UserStatsDTO dto = new UserStatsDTO();

        List<Map<String, Object>> platformRows = baseMapper.selectStatsByPlatform(userId);
        Map<String, UserStatsDTO.StatSummary> platforms = new LinkedHashMap<>();
        int totalSolved = 0;
        int totalSubmitted = 0;
        for (Map<String, Object> row : platformRows) {
            String platform = (String) row.get("platform");
            int solved = ((Number) row.get("solved")).intValue();
            int submitted = ((Number) row.get("submitted")).intValue();
            double rate = submitted > 0
                    ? BigDecimal.valueOf(solved * 100.0 / submitted).setScale(1, RoundingMode.HALF_UP).doubleValue()
                    : 0;
            platforms.put(platform, new UserStatsDTO.StatSummary(solved, submitted, rate));
            totalSolved += solved;
            totalSubmitted += submitted;
        }
        dto.setPlatforms(platforms);

        double totalRate = totalSubmitted > 0
                ? BigDecimal.valueOf(totalSolved * 100.0 / totalSubmitted).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0;
        dto.setTotal(new UserStatsDTO.StatSummary(totalSolved, totalSubmitted, totalRate));

        List<Map<String, Object>> diffRows = baseMapper.selectSolvedByDifficulty(userId);
        Map<String, Map<String, Integer>> difficulties = new LinkedHashMap<>();
        for (Map<String, Object> row : diffRows) {
            String platform = (String) row.get("platform");
            String difficulty = (String) row.get("difficulty");
            int solved = ((Number) row.get("solved")).intValue();
            difficulties.computeIfAbsent(platform, k -> new LinkedHashMap<>()).put(difficulty, solved);
        }
        dto.setDifficulties(difficulties);

        List<Map<String, Object>> dailyRows = baseMapper.selectRecentDaily(userId);
        List<UserStatsDTO.DailyCount> daily = new ArrayList<>();
        for (Map<String, Object> row : dailyRows) {
            String date = row.get("date").toString();
            int count = ((Number) row.get("count")).intValue();
            daily.add(new UserStatsDTO.DailyCount(date, count));
        }
        dto.setRecentDaily(daily);

        return dto;
    }
}
