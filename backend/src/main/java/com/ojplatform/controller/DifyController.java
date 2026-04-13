package com.ojplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojplatform.dto.DifyChatRequest;
import com.ojplatform.dto.DifyChatResponse;
import com.ojplatform.dto.SessionTrackItemDTO;
import com.ojplatform.entity.Problem;
import com.ojplatform.entity.Submission;
import com.ojplatform.service.DifyApiService;
import com.ojplatform.service.PracticeSessionService;
import com.ojplatform.service.ProblemService;
import com.ojplatform.service.SubmissionService;
import com.ojplatform.service.UserProfileService;
import com.ojplatform.service.ProblemSetService;
import com.ojplatform.dto.SmartGenerateDTO;
import com.ojplatform.entity.UserProfile;
import com.ojplatform.entity.ProblemSet;
import com.ojplatform.entity.ProblemSetItem;
import com.ojplatform.entity.Problem;
import com.ojplatform.mapper.ProblemSetItemMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Dify AI 控制器
 * 提供推荐下一题、代码分析、渐进式提示、主动提问四大端点
 * 所有端点均返回 SSE 流：message 事件携带文本片段，done 事件携带完整结果
 */
@RestController
@RequestMapping("/api/dify")
public class DifyController {

    private static final Logger log = LoggerFactory.getLogger(DifyController.class);

    @Autowired
    private DifyApiService difyApiService;

    @Autowired
    private PracticeSessionService sessionService;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private ProblemSetService problemSetService;

    @Autowired
    private ProblemSetItemMapper problemSetItemMapper;

    /** 从 AI 回复中提取 [PROBLEM_SET]...[/PROBLEM_SET] 内的题目 slug 列表 */
    private static final Pattern PROBLEM_SET_BLOCK = Pattern.compile("\\[PROBLEM_SET\\](.*?)\\[/PROBLEM_SET\\]", Pattern.DOTALL);
    private static final Pattern SLUG_IN_LINE = Pattern.compile("^\\d+\\.\\s*([a-z0-9]+(?:-[a-z0-9]+)*)\\s*\\|", Pattern.MULTILINE);

    /**
     * 推荐下一题（SSE 流式）
     * POST /api/dify/recommend-next
     */
    @PostMapping(value = "/recommend-next", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter recommendNext(@RequestBody DifyChatRequest req) {
        SseEmitter emitter = new SseEmitter(180_000L);

        CompletableFuture.runAsync(() -> {
            try {
                List<SessionTrackItemDTO> history = sessionService.getSessionTrack(req.getSessionId());
                Problem currentProblem = problemService.getBySlug(req.getProblemSlug(), req.getOjPlatform());
                String currentDifficulty = (currentProblem != null) ? currentProblem.getDifficulty() : "";
                String currentTopicTags = (currentProblem != null) ? currentProblem.getTopicTags() : "[]";
                List<Submission> submissions = submissionService.getSessionSubmissions(req.getSessionId());
                String submissionSummary = buildSubmissionSummary(history, submissions);

                DifyChatResponse response = difyApiService.recommendNext(
                        req.getSessionId(),
                        req.getProblemSlug(),
                        history,
                        currentDifficulty,
                        currentTopicTags,
                        submissionSummary,
                        req.getOjPlatform(),
                        chunk -> sendChunk(emitter, chunk)
                );

                sendDone(emitter, response);
            } catch (Exception e) {
                sendError(emitter, e);
            }
        });

        return emitter;
    }

    /**
     * 代码分析（SSE 流式）
     * POST /api/dify/analyze
     */
    @PostMapping(value = "/analyze", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter analyze(@RequestBody DifyChatRequest req) {
        SseEmitter emitter = new SseEmitter(180_000L);

        CompletableFuture.runAsync(() -> {
            try {
                Problem problem = problemService.getBySlug(req.getProblemSlug(), req.getOjPlatform());
                String topicTags = (problem != null) ? problem.getTopicTags() : null;

                DifyChatResponse response = difyApiService.analyzeSubmission(
                        req.getSessionId(),
                        req.getProblemSlug(),
                        req.getCode(),
                        req.getLanguage(),
                        req.getJudgeStatus(),
                        req.getRuntime(),
                        req.getMemory(),
                        topicTags,
                        req.getTotalCorrect(),
                        req.getTotalTestcases(),
                        chunk -> sendChunk(emitter, chunk)
                );

                sendDone(emitter, response);
            } catch (Exception e) {
                sendError(emitter, e);
            }
        });

        return emitter;
    }

    /**
     * 渐进式提示（SSE 流式）
     * POST /api/dify/hint
     */
    @PostMapping(value = "/hint", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter hint(@RequestBody DifyChatRequest req) {
        SseEmitter emitter = new SseEmitter(180_000L);

        CompletableFuture.runAsync(() -> {
            try {
                Problem problem = problemService.getBySlug(req.getProblemSlug(), req.getOjPlatform());
                String topicTags = (problem != null) ? problem.getTopicTags() : null;
                String difficulty = (problem != null) ? problem.getDifficulty() : "";

                DifyChatResponse response = difyApiService.requestHint(
                        req.getSessionId(),
                        req.getProblemSlug(),
                        req.getHintLevel(),
                        topicTags,
                        difficulty,
                        chunk -> sendChunk(emitter, chunk)
                );

                sendDone(emitter, response);
            } catch (Exception e) {
                sendError(emitter, e);
            }
        });

        return emitter;
    }

    /**
     * 主动提问（SSE 流式）
     * POST /api/dify/chat
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody DifyChatRequest req) {
        SseEmitter emitter = new SseEmitter(180_000L);

        CompletableFuture.runAsync(() -> {
            try {
                DifyChatResponse response = difyApiService.askTeacher(
                        req.getSessionId(),
                        req.getMessage(),
                        chunk -> sendChunk(emitter, chunk)
                );

                sendDone(emitter, response);
            } catch (Exception e) {
                sendError(emitter, e);
            }
        });

        return emitter;
    }

    // ======================== SSE 发送辅助方法 ========================

    /** 发送文本片段 */
    private void sendChunk(SseEmitter emitter, String chunk) {
        try {
            emitter.send(SseEmitter.event().name("message").data(chunk));
        } catch (IOException e) {
            log.warn("SSE 发送 chunk 失败（客户端可能已断开）");
        }
    }

    /** 发送完成事件（携带完整结果 JSON） */
    private void sendDone(SseEmitter emitter, DifyChatResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            emitter.send(SseEmitter.event().name("done").data(json));
            emitter.complete();
        } catch (IOException e) {
            log.warn("SSE 发送 done 失败");
        }
    }

    /** 发送错误事件 */
    private void sendError(SseEmitter emitter, Exception e) {
        log.error("Dify SSE 处理失败", e);
        try {
            String msg = e.getMessage() != null ? e.getMessage() : "未知错误";
            emitter.send(SseEmitter.event().name("error").data(msg));
            emitter.complete();
        } catch (IOException ignored) {
        }
    }

    // ======================== 私有方法 ========================

    /**
     * 构建会话提交摘要
     * 按题目维度统计尝试次数、是否通过、最终状态
     */
    private String buildSubmissionSummary(List<SessionTrackItemDTO> history, List<Submission> submissions) {
        if (submissions.isEmpty()) return "暂无提交记录";

        Map<Long, List<Submission>> grouped = submissions.stream()
                .collect(Collectors.groupingBy(Submission::getProblemId));

        StringBuilder sb = new StringBuilder();
        for (SessionTrackItemDTO sp : history) {
            List<Submission> subs = grouped.getOrDefault(sp.getProblemId(), List.of());
            if (subs.isEmpty()) {
                sb.append("#").append(sp.getFrontendId()).append(" ").append(sp.getTitle()).append(": 未提交\n");
                continue;
            }
            int attempts = subs.size();
            Submission last = subs.get(subs.size() - 1);
            boolean accepted = subs.stream().anyMatch(s -> "Accepted".equals(s.getStatus()));
            sb.append("#").append(sp.getFrontendId()).append(" ").append(sp.getTitle())
              .append(": 尝试").append(attempts).append("次, ")
              .append(accepted ? "已通过" : "未通过(最后状态:" + last.getStatus() + ")");
            if (last.getTotalCorrect() != null && last.getTotalTestcases() != null) {
                sb.append(" ").append(last.getTotalCorrect()).append("/").append(last.getTotalTestcases());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 获取会话的 Dify 历史消息
     *
     * GET /api/dify/messages?sessionId=xxx
     */
    @GetMapping("/messages")
    public com.ojplatform.common.Result<List<Map<String, Object>>> getMessages(@RequestParam Long sessionId) {
        List<Map<String, Object>> messages = difyApiService.getConversationMessages(sessionId);
        return com.ojplatform.common.Result.ok(messages);
    }

    /**
     * 智能组题（SSE 流式）
     * POST /api/dify/smart-generate
     * 调用 Dify 智能组题应用，根据用户水平和目标推荐题目组合
     * AI 回复后自动解析 slug 列表并创建题单
     */
    @PostMapping(value = "/smart-generate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter smartGenerate(@RequestBody SmartGenerateDTO req, HttpServletRequest httpReq) {
        SseEmitter emitter = new SseEmitter(180_000L);
        Long userId = (Long) httpReq.getAttribute("userId");

        CompletableFuture.runAsync(() -> {
            try {
                // 1. 获取用户画像
                UserProfile profile = userProfileService.getOrCreateProfile(userId);

                // 2. 构建 Dify 输入（题目由 Dify RAG 知识库检索，不再从后端传递）
                Map<String, String> inputs = new HashMap<>();
                inputs.put("count", String.valueOf(req.getCount() != null ? req.getCount() : 10));
                inputs.put("self_assessment", req.getSelfAssessment() != null ? req.getSelfAssessment() : "");
                inputs.put("target_goal", req.getTargetGoal() != null ? req.getTargetGoal() : "");
                inputs.put("preference", req.getPreference() != null ? req.getPreference() : "");
                inputs.put("time_budget", req.getTimeBudget() != null ? req.getTimeBudget() : "");
                inputs.put("user_profile", String.format(
                        "水平: %s, 目标: %s, Easy: %d, Medium: %d, Hard: %d, 通过率: %.1f%%, 擅长: %s, 薄弱: %s",
                        profile.getSkillLevel(),
                        profile.getTargetLevel() != null ? profile.getTargetLevel() : "未设置",
                        profile.getSolvedEasy(), profile.getSolvedMedium(), profile.getSolvedHard(),
                        profile.getAcceptanceRate().doubleValue(),
                        profile.getStrongTags() != null ? profile.getStrongTags() : "[]",
                        profile.getWeakTags() != null ? profile.getWeakTags() : "[]"
                ));

                String query = "请为我组一套 " + inputs.get("count") + " 道题的练习题单。\n"
                        + "我的水平：" + inputs.get("self_assessment") + "\n"
                        + "我的目标：" + inputs.get("target_goal") + "\n"
                        + (req.getPreference() != null ? "偏好方向：" + req.getPreference() + "\n" : "")
                        + (req.getTimeBudget() != null ? "时间预算：" + req.getTimeBudget() + "\n" : "");

                // 4. 调用 Dify
                DifyChatResponse response = difyApiService.smartGenerate(
                        userId, query, inputs,
                        chunk -> sendChunk(emitter, chunk)
                );

                // 5. 从 AI 回复中提取题目并自动创建题单
                if (response.getAnswer() != null) {
                    List<String> slugs = extractProblemSlugs(response.getAnswer());
                    if (!slugs.isEmpty()) {
                        ProblemSet ps = createProblemSetFromSlugs(userId, req, slugs);
                        response.setNextProblemSlug("set:" + ps.getId()); // 用特殊格式标识题单 ID
                    }
                }

                sendDone(emitter, response);
            } catch (Exception e) {
                sendError(emitter, e);
            }
        });

        return emitter;
    }

    /**
     * 赛后分析（SSE 流式）
     * POST /api/dify/contest-analysis
     */
    @PostMapping(value = "/contest-analysis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter contestAnalysis(@RequestBody Map<String, Object> req, HttpServletRequest httpReq) {
        SseEmitter emitter = new SseEmitter(180_000L);
        Long userId = (Long) httpReq.getAttribute("userId");

        CompletableFuture.runAsync(() -> {
            try {
                UserProfile profile = userProfileService.getOrCreateProfile(userId);

                Map<String, String> inputs = new HashMap<>();
                inputs.put("contest_title", String.valueOf(req.getOrDefault("contestTitle", "")));
                inputs.put("scoring_rule", String.valueOf(req.getOrDefault("scoringRule", "acm")));
                inputs.put("user_rank", String.valueOf(req.getOrDefault("userRank", "")));
                inputs.put("total_participants", String.valueOf(req.getOrDefault("totalParticipants", "")));
                inputs.put("problem_results", String.valueOf(req.getOrDefault("problemResults", "[]")));
                inputs.put("user_profile", String.format(
                        "水平: %s, Easy: %d, Medium: %d, Hard: %d",
                        profile.getSkillLevel(),
                        profile.getSolvedEasy(), profile.getSolvedMedium(), profile.getSolvedHard()
                ));

                String query = "请分析我在比赛「" + inputs.get("contest_title") + "」中的表现，"
                        + "我的排名是 " + inputs.get("user_rank") + " / " + inputs.get("total_participants")
                        + "。请给出详细的赛后分析报告。";

                DifyChatResponse response = difyApiService.contestAnalysis(
                        userId, query, inputs,
                        chunk -> sendChunk(emitter, chunk)
                );

                sendDone(emitter, response);
            } catch (Exception e) {
                sendError(emitter, e);
            }
        });

        return emitter;
    }

    /**
     * 从 AI 回复中提取 [PROBLEM_SET] 块内的题目 slug 列表
     */
    private List<String> extractProblemSlugs(String answer) {
        List<String> slugs = new java.util.ArrayList<>();
        Matcher blockMatcher = PROBLEM_SET_BLOCK.matcher(answer);
        if (blockMatcher.find()) {
            String block = blockMatcher.group(1);
            Matcher slugMatcher = SLUG_IN_LINE.matcher(block);
            while (slugMatcher.find()) {
                slugs.add(slugMatcher.group(1).toLowerCase());
            }
        }
        return slugs;
    }

    /**
     * 根据 slug 列表自动创建题单
     */
    private ProblemSet createProblemSetFromSlugs(Long userId, SmartGenerateDTO req, List<String> slugs) {
        String platform = req.getOjPlatform() != null ? req.getOjPlatform() : "leetcode";
        String title = req.getTitle();
        if (title != null) {
            title = title.trim();
        }
        if (title == null || title.isBlank()) {
            title = "AI 智能组题 (" + slugs.size() + "题)";
        }

        ProblemSet ps = new ProblemSet();
        ps.setUserId(userId);
        ps.setTitle(title);
        ps.setSourceType("dify_smart");
        ps.setOjPlatform(platform);
        ps.setVisibility("private");
        ps.setStatus("published");
        ps.setProblemCount(0);
        ps.setTotalScore(0);

        // 保存 Dify 参数快照
        try {
            Map<String, String> params = new HashMap<>();
            params.put("selfAssessment", req.getSelfAssessment());
            params.put("targetGoal", req.getTargetGoal());
            params.put("preference", req.getPreference());
            params.put("count", String.valueOf(req.getCount()));
            ps.setDifyParams(objectMapper.writeValueAsString(params));
        } catch (Exception ignored) {}

        problemSetService.save(ps);

        int seq = 1;
        int totalScore = 0;
        for (String slug : slugs) {
            Problem problem = problemService.getBySlug(slug, platform);
            if (problem == null) {
                log.warn("AI 推荐的题目不存在，跳过：{}", slug);
                continue;
            }
            ProblemSetItem item = new ProblemSetItem();
            item.setSetId(ps.getId());
            item.setProblemId(problem.getId());
            item.setSeqOrder(seq++);
            item.setScore(100);
            problemSetItemMapper.insert(item);
            totalScore += 100;
        }

        ps.setProblemCount(seq - 1);
        ps.setTotalScore(totalScore);
        problemSetService.updateById(ps);

        log.info("AI 智能组题创建题单：id={}, 题目数={}", ps.getId(), seq - 1);
        return ps;
    }
}
