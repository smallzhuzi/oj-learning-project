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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 提交记录服务实现类
 * 负责：保存提交记录 → 转发代码到远程 OJ → 轮询判题结果
 */
@Service
public class SubmissionServiceImpl extends ServiceImpl<SubmissionMapper, Submission> implements SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionServiceImpl.class);

    @Autowired
    private ProblemService problemService;

    @Autowired
    private OjApiServiceFactory ojApiServiceFactory;

    @Override
    public Submission submitCode(SubmitCodeDTO dto) {
        String ojPlatform = dto.getOjPlatform();

        // 1. 查找本地题目记录
        Problem problem = problemService.getBySlug(dto.getProblemSlug(), ojPlatform);
        if (problem == null) {
            throw new RuntimeException("题目不存在：" + dto.getProblemSlug());
        }

        // 2. 创建提交记录（状态为 Pending）
        Submission submission = new Submission();
        submission.setUserId(dto.getUserId());
        submission.setProblemId(problem.getId());
        submission.setSessionId(dto.getSessionId());
        submission.setLanguage(dto.getLanguage());
        submission.setCode(dto.getCode());
        submission.setStatus("Pending");
        baseMapper.insert(submission);

        // 3. 通过策略工厂获取对应平台的 API 服务，提交代码
        OjApiService apiService = ojApiServiceFactory.getService(ojPlatform);
        String platformLang = apiService.mapLanguage(dto.getLanguage());
        String remoteId = apiService.submitCode(
                problem.getSlug(),
                problem.getQuestionId(),
                platformLang,
                dto.getCode()
        );
        submission.setRemoteSubmissionId(remoteId);
        baseMapper.updateById(submission);
        log.info("代码提交成功：ojPlatform={}, problemSlug={}, remoteId={}", ojPlatform, dto.getProblemSlug(), remoteId);

        return submission;
    }

    @Override
    public Submission pollResult(Long submissionId) {
        Submission submission = baseMapper.selectById(submissionId);
        if (submission == null) {
            throw new RuntimeException("提交记录不存在：" + submissionId);
        }

        // 如果没有远程提交 ID，无法轮询
        if (submission.getRemoteSubmissionId() == null) {
            return submission;
        }

        // 如果已经有最终结果，不再轮询
        if (!"Pending".equals(submission.getStatus())) {
            return submission;
        }

        // 反查题目获取 ojPlatform，通过策略工厂路由到对应服务
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
        if (problem == null) return List.of();
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

    @Override
    public UserStatsDTO getUserStats(Long userId) {
        UserStatsDTO dto = new UserStatsDTO();

        // 1. 按平台统计
        List<Map<String, Object>> platformRows = baseMapper.selectStatsByPlatform(userId);
        Map<String, UserStatsDTO.StatSummary> platforms = new LinkedHashMap<>();
        int totalSolved = 0, totalSubmitted = 0;
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

        // 总计
        double totalRate = totalSubmitted > 0
                ? BigDecimal.valueOf(totalSolved * 100.0 / totalSubmitted).setScale(1, RoundingMode.HALF_UP).doubleValue()
                : 0;
        dto.setTotal(new UserStatsDTO.StatSummary(totalSolved, totalSubmitted, totalRate));

        // 2. 按平台+难度统计
        List<Map<String, Object>> diffRows = baseMapper.selectSolvedByDifficulty(userId);
        Map<String, Map<String, Integer>> difficulties = new LinkedHashMap<>();
        for (Map<String, Object> row : diffRows) {
            String platform = (String) row.get("platform");
            String difficulty = (String) row.get("difficulty");
            int solved = ((Number) row.get("solved")).intValue();
            difficulties.computeIfAbsent(platform, k -> new LinkedHashMap<>()).put(difficulty, solved);
        }
        dto.setDifficulties(difficulties);

        // 3. 近 30 天每日提交
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
