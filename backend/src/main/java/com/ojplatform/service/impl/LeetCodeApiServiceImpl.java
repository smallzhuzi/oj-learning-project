package com.ojplatform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ojplatform.common.LeetCodeApiException;
import com.ojplatform.config.LeetCodeConfig;
import com.ojplatform.dto.LeetCodeProblemDetail;
import com.ojplatform.dto.OjJudgeResult;
import com.ojplatform.dto.OjProblemDetail;
import com.ojplatform.entity.Problem;
import com.ojplatform.service.OjApiService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 力扣接口相关业务实现。
 */
@Service
public class LeetCodeApiServiceImpl implements OjApiService {

    private static final Logger log = LoggerFactory.getLogger(LeetCodeApiServiceImpl.class);

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    /** 题目列表 GraphQL 查询语句（leetcode.cn 中国站专用） */
    private static final String PROBLEM_LIST_QUERY = """
            query problemsetQuestionList($categorySlug: String, $limit: Int, $skip: Int, $filters: QuestionListFilterInput) {
                problemsetQuestionList(categorySlug: $categorySlug, limit: $limit, skip: $skip, filters: $filters) {
                    hasMore
                    total
                    questions {
                        acRate
                        difficulty
                        frontendQuestionId
                        titleSlug
                        title
                        topicTags {
                            name
                            slug
                        }
                    }
                }
            }
            """;

    /** 题目详情 GraphQL 查询语句 */
    private static final String PROBLEM_DETAIL_QUERY = """
            query questionData($titleSlug: String!) {
                question(titleSlug: $titleSlug) {
                    questionId
                    questionFrontendId
                    title
                    translatedTitle
                    content
                    translatedContent
                    difficulty
                    stats
                    codeSnippets {
                        lang
                        langSlug
                        code
                    }
                    topicTags {
                        name
                        slug
                    }
                }
            }
            """;

    @Autowired
    private OkHttpClient leetCodeHttpClient;

    @Autowired
    private LeetCodeConfig leetCodeConfig;

    @Autowired
    private ObjectMapper objectMapper;

    // ======================== OjApiService 接口实现 ========================

    @Override
    public String getPlatform() {
        return "leetcode";
    }

    @Override
    public String mapLanguage(String commonLangSlug) {
        // LeetCode 直接使用通用语言 slug（java / python3 / cpp）
        return commonLangSlug;
    }

    @Override
    public List<Problem> fetchProblemList(int skip, int limit, String keyword) {
        log.info("从 LeetCode 拉取题目列表：skip={}, limit={}, keyword={}", skip, limit, keyword);

        // 构建 GraphQL 请求体
        ObjectNode variables = objectMapper.createObjectNode();
        variables.put("categorySlug", "");
        variables.put("limit", limit);
        variables.put("skip", skip);

        ObjectNode filters = objectMapper.createObjectNode();
        if (keyword != null && !keyword.isBlank()) {
            filters.put("searchKeywords", keyword.trim());
        }
        variables.set("filters", filters);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("query", PROBLEM_LIST_QUERY);
        body.set("variables", variables);
        body.put("operationName", "problemsetQuestionList");

        // 发送请求
        JsonNode data = executeGraphQL(body, null);

        // 解析响应
        JsonNode questionList = data.path("problemsetQuestionList").path("questions");
        List<Problem> problems = new ArrayList<>();

        if (questionList.isArray()) {
            for (JsonNode q : questionList) {
                Problem problem = new Problem();
                problem.setFrontendId(q.path("frontendQuestionId").asText());
                problem.setSlug(q.path("titleSlug").asText());
                problem.setTitle(q.path("title").asText());
                problem.setDifficulty(q.path("difficulty").asText());
                // acRate 是小数形式（如 0.495），转为百分比
                double acRate = q.path("acRate").asDouble(0);
                problem.setAcceptanceRate(BigDecimal.valueOf(acRate));
                problems.add(problem);
            }
        }

        log.info("成功拉取 {} 道题目", problems.size());
        return problems;
    }

    @Override
    public OjProblemDetail fetchProblemDetail(String titleSlug) {
        log.info("从 LeetCode 拉取题目详情：slug={}", titleSlug);

        // 构建 GraphQL 请求体
        ObjectNode variables = objectMapper.createObjectNode();
        variables.put("titleSlug", titleSlug);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("query", PROBLEM_DETAIL_QUERY);
        body.set("variables", variables);
        body.put("operationName", "questionData");

        // 发送请求
        JsonNode data = executeGraphQL(body, titleSlug);
        JsonNode question = data.path("question");

        if (question.isMissingNode() || question.isNull()) {
            throw new LeetCodeApiException("LeetCode 上不存在该题目: " + titleSlug);
        }

        // 解析代码模板列表
        List<LeetCodeProblemDetail.CodeSnippet> snippets = new ArrayList<>();
        JsonNode snippetsNode = question.path("codeSnippets");
        if (snippetsNode.isArray()) {
            for (JsonNode sn : snippetsNode) {
                LeetCodeProblemDetail.CodeSnippet snippet = new LeetCodeProblemDetail.CodeSnippet();
                snippet.setLang(sn.path("lang").asText());
                snippet.setLangSlug(sn.path("langSlug").asText());
                snippet.setCode(sn.path("code").asText());
                snippets.add(snippet);
            }
        }

        // 解析题目标签列表
        List<LeetCodeProblemDetail.TopicTag> tags = new ArrayList<>();
        JsonNode tagsNode = question.path("topicTags");
        if (tagsNode.isArray()) {
            for (JsonNode t : tagsNode) {
                LeetCodeProblemDetail.TopicTag tag = new LeetCodeProblemDetail.TopicTag();
                tag.setName(t.path("name").asText());
                tag.setSlug(t.path("slug").asText());
                tags.add(tag);
            }
        }

        // 构造统一的 OjProblemDetail
        OjProblemDetail detail = new OjProblemDetail();
        detail.setQuestionId(question.path("questionId").asText());
        detail.setFrontendId(question.path("questionFrontendId").asText());
        detail.setTitle(question.path("translatedTitle").asText());
        detail.setContent(question.path("translatedContent").asText());
        detail.setDifficulty(question.path("difficulty").asText());
        detail.setStats(question.path("stats").asText());
        detail.setAcceptanceRate(parseAcceptanceRate(question.path("stats").asText()));
        detail.setCodeSnippets(snippets);
        detail.setTopicTags(tags);

        log.info("成功拉取题目详情：{} - {}", detail.getFrontendId(), detail.getTitle());
        return detail;
    }

    @Override
    public String submitCode(String slug, String questionId, String lang, String code) {
        log.info("提交代码到 LeetCode：slug={}, lang={}", slug, lang);

        String url = leetCodeConfig.getBaseUrl() + "/problems/" + slug + "/submit/";

        // 构建请求体
        ObjectNode body = objectMapper.createObjectNode();
        body.put("question_id", questionId);
        body.put("lang", lang);
        body.put("typed_code", code);

        RequestBody requestBody = RequestBody.create(toJsonString(body), JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(slug))
                .post(requestBody)
                .build();

        try (Response response = leetCodeHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);

            // 检查是否包含错误信息
            if (root.has("error")) {
                throw new LeetCodeApiException("LeetCode 提交失败: " + root.path("error").asText());
            }

            String submissionId = root.path("submission_id").asText();
            log.info("代码提交成功，远程提交 ID: {}", submissionId);
            return submissionId;

        } catch (LeetCodeApiException e) {
            throw e;
        } catch (IOException e) {
            throw new LeetCodeApiException("网络连接失败，无法提交代码到 LeetCode", e);
        } catch (Exception e) {
            throw new LeetCodeApiException("提交代码时发生异常: " + e.getMessage(), e);
        }
    }

    @Override
    public OjJudgeResult checkResult(String submissionId) {
        log.debug("轮询判题结果：submissionId={}", submissionId);

        String url = leetCodeConfig.getBaseUrl() + "/submissions/detail/" + submissionId + "/check/";

        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(null))
                .get()
                .build();

        try (Response response = leetCodeHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);

            OjJudgeResult result = new OjJudgeResult();
            String state = root.path("state").asText("PENDING");

            if ("SUCCESS".equals(state)) {
                // 判题完成
                result.setFinished(true);
                result.setStatusMsg(root.path("status_msg").asText());
                result.setRuntime(root.path("status_runtime").asText(null));
                result.setMemory(root.path("status_memory").asText(null));
                result.setTotalCorrect(root.path("total_correct").isNull() ? null : root.path("total_correct").asInt());
                result.setTotalTestcases(root.path("total_testcases").isNull() ? null : root.path("total_testcases").asInt());
                log.info("判题完成：status={}, runtime={}, memory={}", result.getStatusMsg(), result.getRuntime(), result.getMemory());
            } else {
                // 仍在判题中
                result.setFinished(false);
            }

            return result;

        } catch (LeetCodeApiException e) {
            throw e;
        } catch (IOException e) {
            throw new LeetCodeApiException("网络连接失败，无法查询判题结果", e);
        } catch (Exception e) {
            throw new LeetCodeApiException("查询判题结果时发生异常: " + e.getMessage(), e);
        }
    }

    // ======================== 私有方法 ========================

    /**
     * 从 LeetCode stats JSON 字符串中解析通过率
     */
    private BigDecimal parseAcceptanceRate(String statsJson) {
        try {
            JsonNode stats = objectMapper.readTree(statsJson);
            String acRate = stats.path("acRate").asText("0");
            return new BigDecimal(acRate.replace("%", ""));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 执行 GraphQL 请求并返回 data 节点
     */
    private JsonNode executeGraphQL(ObjectNode body, String slug) {
        String url = leetCodeConfig.getBaseUrl() + "/graphql/";

        RequestBody requestBody = RequestBody.create(toJsonString(body), JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(url)
                .headers(buildHeaders(slug))
                .post(requestBody)
                .build();

        try (Response response = leetCodeHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);

            // 检查 GraphQL 级别的错误
            if (root.has("errors") && root.path("errors").isArray() && !root.path("errors").isEmpty()) {
                String errorMsg = root.path("errors").get(0).path("message").asText("未知 GraphQL 错误");
                throw new LeetCodeApiException("LeetCode GraphQL 查询失败: " + errorMsg);
            }

            return root.path("data");

        } catch (LeetCodeApiException e) {
            throw e;
        } catch (IOException e) {
            throw new LeetCodeApiException("网络连接失败，无法访问 LeetCode GraphQL", e);
        } catch (Exception e) {
            throw new LeetCodeApiException("GraphQL 请求异常: " + e.getMessage(), e);
        }
    }

    /**
     * 构造 LeetCode API 请求的通用 Header
     */
    private Headers buildHeaders(String slug) {
        String referer = (slug != null)
                ? leetCodeConfig.getBaseUrl() + "/problems/" + slug + "/"
                : leetCodeConfig.getBaseUrl() + "/problemset/";

        return new Headers.Builder()
                .add("Cookie", leetCodeConfig.getSession())
                .add("x-csrftoken", leetCodeConfig.getCsrfToken())
                .add("Referer", referer)
                .add("Content-Type", "application/json")
                .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                .add("Origin", leetCodeConfig.getBaseUrl())
                .build();
    }

    /**
     * 统一处理 LeetCode API 非成功响应
     */
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
            case 403 -> throw new LeetCodeApiException(
                    "LeetCode 认证失败，请检查 LEETCODE_SESSION 和 csrftoken 是否过期", 403);
            case 429 -> throw new LeetCodeApiException(
                    "LeetCode API 请求频率超限，请稍后再试", 429);
            default -> throw new LeetCodeApiException(
                    "LeetCode API 调用失败 [HTTP " + code + "]: " + body, code);
        }
    }

    /**
     * 将 JsonNode 序列化为 JSON 字符串
     */
    private String toJsonString(ObjectNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new LeetCodeApiException("JSON 序列化失败", e);
        }
    }
}
