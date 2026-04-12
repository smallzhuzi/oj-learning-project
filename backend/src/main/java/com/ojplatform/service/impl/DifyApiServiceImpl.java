package com.ojplatform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ojplatform.config.DifyConfig;
import com.ojplatform.dto.DifyChatResponse;
import com.ojplatform.dto.SessionTrackItemDTO;
import com.ojplatform.entity.PracticeSession;
import com.ojplatform.service.DifyApiService;
import com.ojplatform.service.PracticeSessionService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Dify Chatflow API 调用服务实现
 * 使用 OkHttp 调用 Dify 的 Chat Messages 接口（streaming 模式）
 */
@Service
public class DifyApiServiceImpl implements DifyApiService {

    private static final Logger log = LoggerFactory.getLogger(DifyApiServiceImpl.class);

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    /** 从 AI 回复中提取题目 slug 的多种正则（按优先级排列） */
    private static final Pattern STRUCTURED_SLUG = Pattern.compile("\\[NEXT_SLUG:([a-z0-9]+(?:-[a-z0-9]+)*)\\]", Pattern.CASE_INSENSITIVE);
    private static final Pattern URL_SLUG = Pattern.compile("leetcode\\.cn/problems/([a-z0-9]+(?:-[a-z0-9]+)*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SLUG_PATTERN = Pattern.compile("(?:^|[^a-zA-Z0-9-])([a-z][a-z0-9]*(?:-[a-z0-9]+)+)(?:$|[^a-zA-Z0-9-])");

    @Autowired
    private OkHttpClient leetCodeHttpClient;

    @Autowired
    private DifyConfig difyConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PracticeSessionService sessionService;

    // ======================== 公开方法 ========================

    @Override
    public DifyChatResponse recommendNext(Long sessionId, String currentSlug, List<SessionTrackItemDTO> history,
                                           String currentDifficulty, String currentTopicTags, String submissionSummary,
                                           String ojPlatform, Consumer<String> onChunk) {
        log.info("请求 Dify 推荐下一题：sessionId={}, currentSlug={}, platform={}", sessionId, currentSlug, ojPlatform);

        String platform = ojPlatform != null ? ojPlatform : "leetcode";
        String platformLabel = "luogu".equals(platform) ? "洛谷" : "LeetCode";

        ObjectNode inputs = objectMapper.createObjectNode();
        inputs.put("type", "recommend_next");
        inputs.put("problem_slug", currentSlug);
        inputs.put("difficulty", currentDifficulty != null ? currentDifficulty : "");
        inputs.put("topic_tags", currentTopicTags != null ? currentTopicTags : "[]");
        inputs.put("submission_summary", submissionSummary != null ? submissionSummary : "");
        inputs.put("oj_platform", platform);

        String historyStr = history.stream()
                .map(sp -> "#" + sp.getFrontendId() + " " + sp.getTitle() + "(" + sp.getJumpType() + ")")
                .collect(Collectors.joining(" → "));
        inputs.put("history", historyStr);

        String slugFormat = "luogu".equals(platform)
                ? "请用 [NEXT_SLUG:题号] 格式标注推荐题目（洛谷题号如 P1001、B2001），例如 [NEXT_SLUG:P1001]。"
                : "请用 [NEXT_SLUG:题目slug] 格式标注推荐题目（例如 [NEXT_SLUG:two-sum]）。";

        String query = "当前平台：" + platformLabel + "。\n"
                + "当前题目是 " + currentSlug
                + "（难度：" + (currentDifficulty != null ? currentDifficulty : "未知")
                + "，标签：" + (currentTopicTags != null ? currentTopicTags : "未知") + "）。\n"
                + "历史路径：" + historyStr + "\n"
                + "会话练习摘要：\n" + (submissionSummary != null ? submissionSummary : "暂无") + "\n"
                + "请根据用户的当前水平和薄弱点，推荐一道" + platformLabel + "平台上适合的下一题。\n"
                + slugFormat + "\n"
                + "并简要说明推荐理由。";

        DifyChatResponse response = sendMessageStream(sessionId, query, inputs, onChunk);

        // 从完整回复中提取 slug
        if (response.getAnswer() != null) {
            response.setNextProblemSlug(extractSlug(response.getAnswer()));
        }
        log.info("推荐下一题结果：nextProblemSlug={}, answerLength={}",
                response.getNextProblemSlug(), response.getAnswer() != null ? response.getAnswer().length() : 0);
        return response;
    }

    @Override
    public DifyChatResponse analyzeSubmission(Long sessionId, String problemSlug,
                                               String code, String language,
                                               String status, String runtime, String memory,
                                               String topicTags, Integer totalCorrect, Integer totalTestcases,
                                               Consumer<String> onChunk) {
        log.info("请求 Dify 分析提交：sessionId={}, problemSlug={}, status={}", sessionId, problemSlug, status);

        ObjectNode inputs = objectMapper.createObjectNode();
        inputs.put("type", "submit_analysis");
        inputs.put("problem_slug", problemSlug);
        inputs.put("language", language);
        inputs.put("code", code);
        inputs.put("status", status);
        inputs.put("runtime", runtime != null ? runtime : "");
        inputs.put("memory", memory != null ? memory : "");
        inputs.put("topic_tags", topicTags != null ? topicTags : "[]");
        inputs.put("total_correct", String.valueOf(totalCorrect != null ? totalCorrect : 0));
        inputs.put("total_testcases", String.valueOf(totalTestcases != null ? totalTestcases : 0));

        String tagsInfo = topicTags != null ? topicTags : "未知";
        String query = switch (status) {
            case "Accepted" ->
                "题目 " + problemSlug + "（标签：" + tagsInfo + "），语言 " + language
                + "，已通过，耗时 " + runtime + "，内存 " + memory
                + "。请分析时间/空间复杂度，并给出优化建议。如果耗时较高，请重点分析瓶颈。";
            case "Wrong Answer" ->
                "题目 " + problemSlug + "（标签：" + tagsInfo + "），语言 " + language
                + "，答案错误（通过 " + totalCorrect + "/" + totalTestcases + " 个测试用例）"
                + "。请分析代码中的逻辑漏洞，指出可能遗漏的边界条件或特殊情况。";
            case "Time Limit Exceeded" ->
                "题目 " + problemSlug + "（标签：" + tagsInfo + "），语言 " + language
                + "，超时（通过 " + totalCorrect + "/" + totalTestcases + " 个测试用例）"
                + "。请分析当前算法的时间复杂度，并建议更优的算法思路和数据结构。";
            case "Runtime Error" ->
                "题目 " + problemSlug + "，语言 " + language
                + "，运行时错误。请检查数组越界、空指针、栈溢出、整数溢出等常见问题，给出修正建议。";
            case "Memory Limit Exceeded" ->
                "题目 " + problemSlug + "，语言 " + language
                + "，内存超限。请分析空间复杂度，建议减少内存占用的方案。";
            case "Compile Error" ->
                "题目 " + problemSlug + "，语言 " + language
                + "，编译错误。请检查语法问题并给出修正建议。";
            default ->
                "题目 " + problemSlug + "，语言 " + language
                + "，结果：" + status + "。请分析可能的原因并给出建议。";
        };

        return sendMessageStream(sessionId, query, inputs, onChunk);
    }

    @Override
    public DifyChatResponse requestHint(Long sessionId, String problemSlug,
                                         Integer hintLevel, String topicTags, String difficulty,
                                         Consumer<String> onChunk) {
        log.info("请求渐进式提示：sessionId={}, slug={}, level={}", sessionId, problemSlug, hintLevel);

        ObjectNode inputs = objectMapper.createObjectNode();
        inputs.put("type", "hint");
        inputs.put("problem_slug", problemSlug);
        inputs.put("hint_level", String.valueOf(hintLevel != null ? hintLevel : 1));
        inputs.put("topic_tags", topicTags != null ? topicTags : "[]");
        inputs.put("difficulty", difficulty != null ? difficulty : "");

        String levelDesc = switch (hintLevel != null ? hintLevel : 1) {
            case 1 -> "请给出这道题的思路方向，提示应该从哪个方向思考，不要给出具体算法或代码。";
            case 2 -> "请给出解题的关键步骤，说明算法的核心逻辑和需要注意的要点，但不要给出完整代码。";
            case 3 -> "请给出伪代码框架，包含主要函数结构和关键逻辑的伪代码实现。";
            default -> "请给出解题提示。";
        };

        String query = "题目 " + problemSlug + "（难度：" + difficulty + "，标签：" + topicTags + "）。" + levelDesc;

        return sendMessageStream(sessionId, query, inputs, onChunk);
    }

    @Override
    public DifyChatResponse askTeacher(Long sessionId, String question, Consumer<String> onChunk) {
        log.info("用户主动提问：sessionId={}", sessionId);

        ObjectNode inputs = objectMapper.createObjectNode();
        inputs.put("type", "ask_teacher");

        return sendMessageStream(sessionId, question, inputs, onChunk);
    }

    // ======================== 核心私有方法 ========================

    /**
     * 流式发送消息到 Dify Chat Messages API
     * 读取 SSE 事件流，每收到一个文本片段就回调 onChunk
     * 自动管理 conversation_id 的创建和复用
     */
    private DifyChatResponse sendMessageStream(Long sessionId, String query, ObjectNode inputs, Consumer<String> onChunk) {
        PracticeSession session = sessionService.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("练习会话不存在：" + sessionId);
        }
        String conversationId = session.getDifyConversationId();

        // 构建请求体（streaming 模式）
        ObjectNode body = objectMapper.createObjectNode();
        body.set("inputs", inputs);
        body.put("query", query);
        body.put("response_mode", "streaming");
        body.put("user", "user-" + session.getUserId());
        if (conversationId != null && !conversationId.isBlank()) {
            body.put("conversation_id", conversationId);
        }

        String url = difyConfig.getBaseUrl() + "/chat-messages";
        RequestBody requestBody = RequestBody.create(toJsonString(body), JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + difyConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        // 使用更长的读取超时（流式响应可能持续很久）
        OkHttpClient streamingClient = leetCodeHttpClient.newBuilder()
                .readTimeout(120, TimeUnit.SECONDS)
                .build();

        StringBuilder fullAnswer = new StringBuilder();
        String newConversationId = null;
        String messageId = null;

        try (Response response = streamingClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }

            // 逐行读取 SSE 事件流
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ")) continue;

                String json = line.substring(6).trim();
                if (json.isEmpty()) continue;

                try {
                    JsonNode node = objectMapper.readTree(json);
                    String event = node.path("event").asText("");

                    if ("message".equals(event)) {
                        // 增量文本片段
                        String chunk = node.path("answer").asText("");
                        fullAnswer.append(chunk);
                        if (newConversationId == null) {
                            newConversationId = node.path("conversation_id").asText(null);
                        }
                        // 回调前端
                        if (onChunk != null && !chunk.isEmpty()) {
                            onChunk.accept(chunk);
                        }
                    } else if ("message_end".equals(event)) {
                        messageId = node.path("id").asText(null);
                        if (newConversationId == null) {
                            newConversationId = node.path("conversation_id").asText(null);
                        }
                    } else if ("error".equals(event)) {
                        String errorMsg = node.path("message").asText("Dify 返回未知错误");
                        throw new RuntimeException("Dify 错误: " + errorMsg);
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    log.warn("解析 SSE 事件失败：{}", line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("网络连接失败，无法访问 Dify API", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Dify API 调用异常: " + e.getMessage(), e);
        }

        // 首次对话绑定 conversation_id
        if ((conversationId == null || conversationId.isBlank()) && newConversationId != null) {
            sessionService.bindDifyConversation(sessionId, newConversationId);
            log.info("绑定 Dify conversation_id：sessionId={}, conversationId={}", sessionId, newConversationId);
        }

        DifyChatResponse result = new DifyChatResponse();
        result.setMessageId(messageId);
        result.setConversationId(newConversationId);
        result.setAnswer(fullAnswer.toString());
        result.setCreatedAt(System.currentTimeMillis() / 1000);

        log.info("Dify 流式回复完成：messageId={}, answerLength={}", messageId, fullAnswer.length());
        return result;
    }

    /**
     * 从 AI 回复中提取题目 slug（多策略匹配）
     * 优先级：结构化标签 > LeetCode URL > slug 模式
     */
    private String extractSlug(String answer) {
        if (answer == null || answer.isBlank()) return null;

        // 策略1：结构化格式 [NEXT_SLUG:xxx]
        Matcher m1 = STRUCTURED_SLUG.matcher(answer);
        if (m1.find()) {
            String slug = m1.group(1).toLowerCase();
            log.info("通过结构化标签提取 slug: {}", slug);
            return slug;
        }

        // 策略2：从 LeetCode URL 中提取
        Matcher m2 = URL_SLUG.matcher(answer);
        if (m2.find()) {
            String slug = m2.group(1).toLowerCase();
            log.info("通过 URL 提取 slug: {}", slug);
            return slug;
        }

        // 策略3：slug 模式（至少含一个连字符的小写串）
        Matcher m3 = SLUG_PATTERN.matcher(answer);
        if (m3.find()) {
            String slug = m3.group(1);
            log.info("通过 slug 模式提取: {}", slug);
            return slug;
        }

        log.warn("无法从 AI 回复中提取题目 slug，回复前200字: {}",
                answer.length() > 200 ? answer.substring(0, 200) + "..." : answer);
        return null;
    }

    /** 统一处理 Dify API 非成功响应 */
    private void handleErrorResponse(Response response) {
        int code = response.code();
        String body = "";
        try {
            if (response.body() != null) {
                body = response.body().string();
            }
        } catch (IOException ignored) {
        }

        switch (code) {
            case 401 -> throw new RuntimeException("Dify API Key 无效或已过期");
            case 404 -> throw new RuntimeException("Dify API 地址不正确，请检查 DIFY_BASE_URL 配置");
            case 429 -> throw new RuntimeException("Dify API 请求频率超限，请稍后再试");
            default -> throw new RuntimeException("Dify API 调用失败 [HTTP " + code + "]: " + body);
        }
    }

    @Override
    public List<Map<String, Object>> getConversationMessages(Long sessionId) {
        PracticeSession session = sessionService.getById(sessionId);
        if (session == null || session.getDifyConversationId() == null) {
            return List.of();
        }

        String conversationId = session.getDifyConversationId();
        String user = "user-" + session.getUserId();
        String url = difyConfig.getBaseUrl() + "/messages?conversation_id=" + conversationId
                + "&user=" + user + "&limit=100";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + difyConfig.getApiKey())
                .get()
                .build();

        try (Response response = leetCodeHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.warn("获取 Dify 历史消息失败：HTTP {}", response.code());
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response.body().string());
            JsonNode data = root.path("data");
            if (!data.isArray()) return List.of();

            List<Map<String, Object>> messages = new ArrayList<>();
            // Dify 返回的消息是从新到旧排列，需要反转
            for (int i = data.size() - 1; i >= 0; i--) {
                JsonNode msg = data.get(i);
                String query = msg.path("query").asText("");
                String answer = msg.path("answer").asText("");
                long createdAt = msg.path("created_at").asLong(0);

                if (!query.isEmpty()) {
                    Map<String, Object> userMsg = new HashMap<>();
                    userMsg.put("role", "user");
                    userMsg.put("content", query);
                    userMsg.put("timestamp", createdAt * 1000);
                    messages.add(userMsg);
                }
                if (!answer.isEmpty()) {
                    Map<String, Object> assistantMsg = new HashMap<>();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", answer);
                    assistantMsg.put("timestamp", createdAt * 1000);
                    messages.add(assistantMsg);
                }
            }
            return messages;
        } catch (Exception e) {
            log.warn("获取 Dify 历史消息异常：{}", e.getMessage());
            return List.of();
        }
    }

    /** JSON 序列化 */
    private String toJsonString(ObjectNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }

    // ======================== 智能组题 & 赛后分析 ========================

    @Override
    public DifyChatResponse smartGenerate(Long userId, String query, Map<String, String> inputs, Consumer<String> onChunk) {
        String apiKey = difyConfig.getSmartGenerateKey();
        if (apiKey == null || apiKey.isBlank()) {
            // 如果没有独立的组题 Key，降级使用主 Key
            apiKey = difyConfig.getApiKey();
        }
        log.info("请求 Dify 智能组题：userId={}", userId);
        return sendStandaloneStream(apiKey, userId, query, inputs, onChunk);
    }

    @Override
    public DifyChatResponse contestAnalysis(Long userId, String query, Map<String, String> inputs, Consumer<String> onChunk) {
        String apiKey = difyConfig.getContestAnalysisKey();
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = difyConfig.getApiKey();
        }
        log.info("请求 Dify 赛后分析：userId={}", userId);
        return sendStandaloneStream(apiKey, userId, query, inputs, onChunk);
    }

    /**
     * 独立应用的流式调用（不绑定 practice_session，不管理 conversation_id）
     */
    private DifyChatResponse sendStandaloneStream(String apiKey, Long userId, String query,
                                                    Map<String, String> inputs, Consumer<String> onChunk) {
        ObjectNode body = objectMapper.createObjectNode();
        ObjectNode inputsNode = objectMapper.createObjectNode();
        if (inputs != null) {
            inputs.forEach(inputsNode::put);
        }
        body.set("inputs", inputsNode);
        body.put("query", query);
        body.put("response_mode", "streaming");
        body.put("user", "user-" + userId);

        String url = difyConfig.getBaseUrl() + "/chat-messages";
        RequestBody requestBody = RequestBody.create(toJsonString(body), JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        OkHttpClient streamingClient = leetCodeHttpClient.newBuilder()
                .readTimeout(120, TimeUnit.SECONDS)
                .build();

        StringBuilder fullAnswer = new StringBuilder();
        String conversationId = null;
        String messageId = null;

        try (Response response = streamingClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8));

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ")) continue;
                String json = line.substring(6).trim();
                if (json.isEmpty()) continue;

                try {
                    JsonNode node = objectMapper.readTree(json);
                    String event = node.path("event").asText("");

                    if ("message".equals(event)) {
                        String chunk = node.path("answer").asText("");
                        fullAnswer.append(chunk);
                        if (conversationId == null) {
                            conversationId = node.path("conversation_id").asText(null);
                        }
                        if (onChunk != null && !chunk.isEmpty()) {
                            onChunk.accept(chunk);
                        }
                    } else if ("message_end".equals(event)) {
                        messageId = node.path("id").asText(null);
                        if (conversationId == null) {
                            conversationId = node.path("conversation_id").asText(null);
                        }
                    } else if ("error".equals(event)) {
                        String errorMsg = node.path("message").asText("Dify 返回未知错误");
                        throw new RuntimeException("Dify 错误: " + errorMsg);
                    }
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    log.warn("解析 SSE 事件失败：{}", line);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("网络连接失败，无法访问 Dify API", e);
        }

        DifyChatResponse result = new DifyChatResponse();
        result.setMessageId(messageId);
        result.setConversationId(conversationId);
        result.setAnswer(fullAnswer.toString());
        result.setCreatedAt(System.currentTimeMillis() / 1000);

        log.info("Dify 独立应用流式回复完成：messageId={}, answerLength={}", messageId, fullAnswer.length());
        return result;
    }
}
