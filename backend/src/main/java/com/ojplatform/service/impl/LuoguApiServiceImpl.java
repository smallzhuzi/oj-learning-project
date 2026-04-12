package com.ojplatform.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojplatform.common.OjApiException;
import com.ojplatform.config.LuoguConfig;
import com.ojplatform.dto.LeetCodeProblemDetail;
import com.ojplatform.dto.OjJudgeResult;
import com.ojplatform.dto.OjProblemDetail;
import com.ojplatform.entity.Problem;
import com.ojplatform.service.OjApiService;
import com.ojplatform.util.LuoguDifficultyMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 洛谷 API 调用服务实现
 * 使用 OkHttp 调用 www.luogu.com.cn 的 REST 接口
 * 通过 _contentOnly=1 参数获取 JSON 数据
 */
@Service
public class LuoguApiServiceImpl implements OjApiService {

    private static final Logger log = LoggerFactory.getLogger(LuoguApiServiceImpl.class);

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    /** 通用语言 slug → 洛谷语言代码映射 */
    private static final Map<String, String> LANG_MAP = Map.of(
            "c", "0",
            "cpp", "11",       // C++17
            "pascal", "2",
            "java", "28",      // Java17
            "python3", "4"
    );

    /** 洛谷评测状态码 → 状态描述 */
    private static final Map<Integer, String> STATUS_MAP = Map.ofEntries(
            Map.entry(0, "Waiting"),
            Map.entry(1, "Judging"),
            Map.entry(2, "Compile Error"),
            Map.entry(3, "Output Limit Exceeded"),
            Map.entry(4, "Memory Limit Exceeded"),
            Map.entry(5, "Time Limit Exceeded"),
            Map.entry(6, "Wrong Answer"),
            Map.entry(7, "Runtime Error"),
            Map.entry(12, "Accepted"),
            Map.entry(14, "Unknown Error")
    );

    /** CSRF Token 缓存 */
    private volatile String cachedCsrfToken;
    private volatile long csrfTokenExpireTime;

    /** 洛谷标签 ID → 中文名映射缓存 */
    private volatile Map<Integer, String> cachedTagMap;
    private volatile long tagMapExpireTime;

    /** 洛谷 CDN 反爬 C3VK cookie 缓存 */
    private volatile String cachedC3VK;

    @Autowired
    private OkHttpClient luoguHttpClient;

    @Autowired
    private LuoguConfig luoguConfig;

    @Autowired
    private LuoguConfig.LuoguCookieJar luoguCookieJar;

    @Autowired
    private ObjectMapper objectMapper;

    // ======================== OjApiService 接口实现 ========================

    @Override
    public String getPlatform() {
        return "luogu";
    }

    @Override
    public String mapLanguage(String commonLangSlug) {
        String mapped = LANG_MAP.get(commonLangSlug);
        if (mapped == null) {
            throw new OjApiException("洛谷不支持的编程语言: " + commonLangSlug, "luogu");
        }
        return mapped;
    }

    @Override
    public List<Problem> fetchProblemList(int skip, int limit, String keyword) {
        // 洛谷分页从 1 开始，每页固定数量
        int page = skip / Math.max(limit, 1) + 1;
        log.info("从洛谷拉取题目列表：page={}, keyword={}", page, keyword);

        // 构建 URL
        StringBuilder urlBuilder = new StringBuilder(luoguConfig.getBaseUrl())
                .append("/problem/list?")
                .append("page=").append(page)
                .append("&type=luogu");
        if (keyword != null && !keyword.isBlank()) {
            urlBuilder.append("&keyword=").append(keyword.trim());
        }

        Request request = new Request.Builder()
                .url(urlBuilder.toString())
                .headers(buildContentOnlyHeaders())
                .get()
                .build();

        try (Response response = luoguHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode problems = root.path("data").path("problems");
            JsonNode result = problems.path("result");

            List<Problem> list = new ArrayList<>();
            if (result.isArray()) {
                for (JsonNode p : result) {
                    Problem problem = new Problem();
                    String pid = p.path("pid").asText();
                    problem.setSlug(pid);
                    problem.setFrontendId(pid);
                    problem.setTitle(p.path("title").asText());
                    // 映射难度
                    int diff = p.path("difficulty").asInt(0);
                    problem.setDifficulty(LuoguDifficultyMapper.toLabel(diff));
                    // 计算通过率
                    long totalAccepted = p.path("totalAccepted").asLong(0);
                    long totalSubmit = p.path("totalSubmit").asLong(1);
                    if (totalSubmit > 0) {
                        BigDecimal rate = BigDecimal.valueOf(totalAccepted * 100.0 / totalSubmit)
                                .setScale(2, RoundingMode.HALF_UP);
                        problem.setAcceptanceRate(rate);
                    }
                    problem.setOjPlatform("luogu");
                    list.add(problem);
                }
            }

            log.info("成功拉取 {} 道洛谷题目", list.size());
            return list;

        } catch (OjApiException e) {
            throw e;
        } catch (IOException e) {
            log.error("访问洛谷网络异常", e);
            throw new OjApiException("网络连接失败，无法访问洛谷: " + e.getMessage(), "luogu", e);
        } catch (Exception e) {
            log.error("拉取洛谷题目列表异常", e);
            throw new OjApiException("拉取洛谷题目列表异常: " + e.getMessage(), "luogu", e);
        }
    }

    @Override
    public OjProblemDetail fetchProblemDetail(String pid) {
        log.info("从洛谷拉取题目详情：pid={}", pid);

        String url = luoguConfig.getBaseUrl() + "/problem/" + pid;

        Request request = new Request.Builder()
                .url(url)
                .headers(buildContentOnlyHeaders())
                .get()
                .build();

        try (Response response = luoguHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode problem = root.path("data").path("problem");

            if (problem.isMissingNode() || problem.isNull()) {
                throw new OjApiException("洛谷上不存在该题目: " + pid, "luogu");
            }

            OjProblemDetail detail = new OjProblemDetail();
            detail.setQuestionId(pid);
            detail.setFrontendId(pid);
            detail.setTitle(problem.path("title").asText());

            // 难度映射
            int diff = problem.path("difficulty").asInt(0);
            detail.setDifficulty(LuoguDifficultyMapper.toLabel(diff));

            // 计算通过率
            long totalAccepted = problem.path("totalAccepted").asLong(0);
            long totalSubmit = problem.path("totalSubmit").asLong(1);
            if (totalSubmit > 0) {
                detail.setAcceptanceRate(BigDecimal.valueOf(totalAccepted * 100.0 / totalSubmit)
                        .setScale(2, RoundingMode.HALF_UP));
            }

            // 拼装题目描述为完整 Markdown
            detail.setContent(buildMarkdownContent(problem));

            // 洛谷不提供代码模板
            detail.setCodeSnippets(Collections.emptyList());

            // 解析标签：data.problem.tags 是数字 ID 数组，通过 /_lfe/tags/zh-CN 映射为中文名
            JsonNode tagsNode = problem.path("tags");
            if (tagsNode.isArray() && tagsNode.size() > 0) {
                Map<Integer, String> tagMap = fetchTagMap();
                List<LeetCodeProblemDetail.TopicTag> tags = new ArrayList<>();
                for (JsonNode t : tagsNode) {
                    int tagId = t.asInt(-1);
                    String tagName = tagMap.getOrDefault(tagId, String.valueOf(tagId));
                    LeetCodeProblemDetail.TopicTag tag = new LeetCodeProblemDetail.TopicTag();
                    tag.setName(tagName);
                    tag.setSlug(String.valueOf(tagId));
                    tags.add(tag);
                }
                detail.setTopicTags(tags);
            } else {
                detail.setTopicTags(Collections.emptyList());
            }

            log.info("成功拉取洛谷题目详情：{} - {}", pid, detail.getTitle());
            return detail;

        } catch (OjApiException e) {
            throw e;
        } catch (IOException e) {
            throw new OjApiException("网络连接失败，无法访问洛谷", "luogu", e);
        } catch (Exception e) {
            throw new OjApiException("拉取洛谷题目详情异常: " + e.getMessage(), "luogu", e);
        }
    }

    @Override
    public String submitCode(String pid, String questionId, String lang, String code) {
        log.info("提交代码到洛谷：pid={}, lang={}", pid, lang);

        // 确保有 C3VK cookie（洛谷 CDN 反爬要求）
        ensureC3VK();

        // 获取动态 CSRF Token
        String csrfToken = fetchCsrfToken();

        String url = luoguConfig.getBaseUrl() + "/fe/api/problem/submit/" + pid;

        // 构建请求体（与浏览器 F12 一致）
        Map<String, Object> bodyMap = new LinkedHashMap<>();
        bodyMap.put("code", code);
        bodyMap.put("lang", Integer.parseInt(lang));
        bodyMap.put("enableO2", 1);

        String bodyJson;
        try {
            bodyJson = objectMapper.writeValueAsString(bodyMap);
        } catch (Exception e) {
            throw new OjApiException("JSON 序列化失败", "luogu", e);
        }

        RequestBody requestBody = RequestBody.create(bodyJson, JSON_MEDIA_TYPE);
        Headers submitHeaders = buildSubmitHeaders(csrfToken);

        // 调试：查看 CookieJar 实际提供的 cookie
        okhttp3.HttpUrl submitUrl = okhttp3.HttpUrl.parse(url);
        log.info("[提交调试] CookieJar cookies: {}", luoguCookieJar.loadForRequest(submitUrl));
        log.info("[提交调试] X-CSRF-TOKEN: {}", submitHeaders.get("x-csrf-token"));
        log.info("[提交调试] 请求体: {}", bodyJson);

        Request request = new Request.Builder()
                .url(url)
                .headers(submitHeaders)
                .post(requestBody)
                .build();

        try (Response response = luoguHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }

            String responseBody = response.body().string();
            JsonNode root = objectMapper.readTree(responseBody);

            // 洛谷返回格式：{ "rid": 12345678 } 或 { "status": 200, "data": { "rid": ... } }
            String rid;
            if (root.has("rid")) {
                rid = root.path("rid").asText();
            } else if (root.path("data").has("rid")) {
                rid = root.path("data").path("rid").asText();
            } else {
                String errorMsg = root.path("errorMessage").asText(
                        root.path("data").asText("未知错误"));
                throw new OjApiException("洛谷提交失败: " + errorMsg, "luogu");
            }

            log.info("代码提交成功，洛谷 record ID: {}", rid);
            return rid;

        } catch (OjApiException e) {
            throw e;
        } catch (IOException e) {
            throw new OjApiException("网络连接失败，无法提交代码到洛谷", "luogu", e);
        } catch (Exception e) {
            throw new OjApiException("提交代码到洛谷异常: " + e.getMessage(), "luogu", e);
        }
    }

    @Override
    public OjJudgeResult checkResult(String rid) {
        log.debug("轮询洛谷判题结果：rid={}", rid);

        String url = luoguConfig.getBaseUrl() + "/record/" + rid;

        // 先尝试用普通页面请求，从嵌入 JSON 中提取数据
        Request request = new Request.Builder()
                .url(url)
                .headers(buildContentOnlyHeaders())
                .get()
                .build();

        try (Response response = luoguHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }

            String responseBody = response.body().string();
            JsonNode root;

            // 如果返回 HTML 页面，从嵌入的 JSON 中提取
            if (responseBody.trim().startsWith("<")) {
                log.debug("[洛谷结果] 收到 HTML，尝试从页面提取 JSON");
                root = extractJsonFromHtml(responseBody);
                if (root == null || root.isMissingNode()) {
                    throw new OjApiException("无法从洛谷页面提取判题数据", "luogu");
                }
            } else {
                root = objectMapper.readTree(responseBody);
            }

            // 洛谷数据结构：currentData.record 或 data.record
            JsonNode record = root.path("currentData").path("record");
            if (record.isMissingNode()) {
                record = root.path("data").path("record");
            }
            JsonNode detail = record.path("detail");

            OjJudgeResult result = new OjJudgeResult();
            int status = record.path("status").asInt(-1);

            if (status == 0 || status == 1) {
                // Waiting 或 Judging，未完成
                result.setFinished(false);
                return result;
            }

            // 判题完成
            result.setFinished(true);
            result.setStatusMsg(STATUS_MAP.getOrDefault(status, "Unknown Error"));

            // 运行时间和内存
            int time = record.path("time").asInt(0);
            int memory = record.path("memory").asInt(0);
            result.setRuntime(time + " ms");
            result.setMemory(formatMemory(memory));

            // 统计通过的测试点
            if (detail.has("judgeResult") && detail.path("judgeResult").has("subtasks")) {
                int correct = 0;
                int total = 0;
                JsonNode subtasks = detail.path("judgeResult").path("subtasks");
                for (JsonNode subtask : subtasks) {
                    JsonNode testCases = subtask.path("testCases");
                    if (testCases.isArray()) {
                        for (JsonNode tc : testCases) {
                            total++;
                            if (tc.path("status").asInt(-1) == 12) {
                                correct++;
                            }
                        }
                    }
                }
                result.setTotalCorrect(correct);
                result.setTotalTestcases(total);
            }

            log.info("洛谷判题完成：rid={}, status={}, runtime={}, memory={}",
                    rid, result.getStatusMsg(), result.getRuntime(), result.getMemory());
            return result;

        } catch (OjApiException e) {
            throw e;
        } catch (IOException e) {
            throw new OjApiException("网络连接失败，无法查询洛谷判题结果", "luogu", e);
        } catch (Exception e) {
            throw new OjApiException("查询洛谷判题结果异常: " + e.getMessage(), "luogu", e);
        }
    }

    // ======================== 私有方法 ========================

    /**
     * 从洛谷 HTML 页面中提取嵌入的 JSON 数据
     * 洛谷将数据嵌入在 <script id="lentille-context" type="application/json"> 标签中
     */
    private JsonNode extractJsonFromHtml(String html) {
        // 查找 lentille-context 脚本标签
        String startMarker = "\"application/json\">";
        int idx = html.indexOf("lentille-context");
        if (idx == -1) {
            throw new OjApiException("无法从洛谷页面中提取数据（未找到 lentille-context）", "luogu");
        }
        int jsonStart = html.indexOf(startMarker, idx);
        if (jsonStart == -1) {
            throw new OjApiException("无法从洛谷页面中提取数据（未找到 JSON 起始）", "luogu");
        }
        jsonStart += startMarker.length();
        int jsonEnd = html.indexOf("</script>", jsonStart);
        if (jsonEnd == -1) {
            throw new OjApiException("无法从洛谷页面中提取数据（未找到 JSON 结束）", "luogu");
        }

        String json = html.substring(jsonStart, jsonEnd);
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new OjApiException("解析洛谷嵌入 JSON 失败: " + e.getMessage(), "luogu", e);
        }
    }

    /**
     * 获取洛谷标签 ID → 中文名映射（带 1 小时缓存）
     * 接口：GET /_lfe/tags/zh-CN
     * 返回数组，每个元素含 id 和 name 字段
     */
    public Map<Integer, String> fetchTagMap() {
        long now = System.currentTimeMillis();
        if (cachedTagMap != null && now < tagMapExpireTime) {
            return cachedTagMap;
        }

        String url = luoguConfig.getBaseUrl() + "/_lfe/tags/zh-CN";
        Request request = new Request.Builder()
                .url(url)
                .headers(buildContentOnlyHeaders())
                .get()
                .build();

        try (Response response = luoguHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("获取洛谷标签映射失败: HTTP {}", response.code());
                return cachedTagMap != null ? cachedTagMap : Collections.emptyMap();
            }

            String body = response.body().string();
            JsonNode root = objectMapper.readTree(body);

            Map<Integer, String> tagMap = new HashMap<>();
            // 响应可能是数组或嵌套在某个字段中
            JsonNode tagsArray = root.isArray() ? root : root.path("data");
            if (!tagsArray.isArray()) {
                // 尝试遍历所有顶层字段，找到数组
                for (JsonNode field : root) {
                    if (field.isArray()) {
                        for (JsonNode tag : field) {
                            int id = tag.path("id").asInt(-1);
                            String name = tag.path("name").asText("");
                            if (id >= 0 && !name.isEmpty()) {
                                tagMap.put(id, name);
                            }
                        }
                    }
                }
            } else {
                for (JsonNode tag : tagsArray) {
                    int id = tag.path("id").asInt(-1);
                    String name = tag.path("name").asText("");
                    if (id >= 0 && !name.isEmpty()) {
                        tagMap.put(id, name);
                    }
                }
            }

            cachedTagMap = tagMap;
            tagMapExpireTime = now + 60 * 60 * 1000; // 1 小时缓存
            log.info("获取洛谷标签映射成功，共 {} 个标签", tagMap.size());
            return tagMap;

        } catch (Exception e) {
            log.warn("获取洛谷标签映射异常: {}", e.getMessage());
            return cachedTagMap != null ? cachedTagMap : Collections.emptyMap();
        }
    }

    /**
     * 拼装洛谷题目描述为完整 Markdown
     * 将 contenu 子对象的 background、description、formatI、formatO、samples、hint 合并为 Markdown
     * 洛谷的题目内容嵌套在 problem.contenu 中，不在顶层
     */
    private String buildMarkdownContent(JsonNode problem) {
        // 优先从 contenu（中文内容）取，若无则从顶层取
        JsonNode contenu = problem.path("contenu");
        if (contenu.isMissingNode() || contenu.isNull()) {
            contenu = problem;
        }

        StringBuilder sb = new StringBuilder();

        String background = contenu.path("background").asText("");
        if (!background.isBlank()) {
            sb.append("## 题目背景\n\n").append(background).append("\n\n");
        }

        String description = contenu.path("description").asText("");
        if (!description.isBlank()) {
            sb.append("## 题目描述\n\n").append(description).append("\n\n");
        }

        String inputFormat = contenu.path("formatI").asText("");
        if (!inputFormat.isBlank()) {
            sb.append("## 输入格式\n\n").append(inputFormat).append("\n\n");
        }

        String outputFormat = contenu.path("formatO").asText("");
        if (!outputFormat.isBlank()) {
            sb.append("## 输出格式\n\n").append(outputFormat).append("\n\n");
        }

        // 样例数据（samples 在 problem 顶层，不在 contenu 中）
        JsonNode samples = problem.path("samples");
        if (samples.isArray() && !samples.isEmpty()) {
            sb.append("## 输入输出样例\n\n");
            for (int i = 0; i < samples.size(); i++) {
                JsonNode sample = samples.get(i);
                if (sample.isArray() && sample.size() >= 2) {
                    sb.append("### 输入 #").append(i + 1).append("\n\n");
                    sb.append("```\n").append(sample.get(0).asText("")).append("\n```\n\n");
                    sb.append("### 输出 #").append(i + 1).append("\n\n");
                    sb.append("```\n").append(sample.get(1).asText("")).append("\n```\n\n");
                }
            }
        }

        String hint = contenu.path("hint").asText("");
        if (!hint.isBlank()) {
            sb.append("## 提示\n\n").append(hint).append("\n\n");
        }

        return sb.toString();
    }

    /**
     * 确保已获取 C3VK cookie（洛谷 CDN 反爬）
     * 请求首页触发 C3VK 挑战脚本，提取 cookie 值
     */
    private void ensureC3VK() {
        if (cachedC3VK != null && !cachedC3VK.isBlank()) {
            return;
        }
        try {
            String url = luoguConfig.getBaseUrl() + "/";
            Request request = new Request.Builder()
                    .url(url)
                    .headers(buildHeaders(false))
                    .get()
                    .build();

            try (Response response = luoguHttpClient.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    if (body.contains("C3VK=")) {
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("C3VK=([a-zA-Z0-9]+)").matcher(body);
                        if (m.find()) {
                            cachedC3VK = m.group(1);
                            luoguCookieJar.addCookie("www.luogu.com.cn", "C3VK", cachedC3VK);
                            log.info("获取 C3VK cookie 成功: {}", cachedC3VK);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取 C3VK 失败: {}", e.getMessage());
        }
    }

    /**
     * 获取洛谷 CSRF Token（带 5 分钟缓存）
     * 流程：请求页面 → 遇到 C3VK 挑战则提取并重试 → 从 lentille-context JSON 中获取真实 token
     */
    private String fetchCsrfToken() {
        long now = System.currentTimeMillis();
        if (cachedCsrfToken != null && now < csrfTokenExpireTime) {
            return cachedCsrfToken;
        }

        // 请求一个具体题目页面（比首页更可靠，首页可能有额外重定向）
        String targetUrl = luoguConfig.getBaseUrl() + "/problem/P1000";

        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                Request request = new Request.Builder()
                        .url(targetUrl)
                        .headers(buildHeaders(false))
                        .get()
                        .build();

                try (Response response = luoguHttpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.warn("[CSRF] 请求失败: HTTP {}", response.code());
                        continue;
                    }

                    String body = response.body().string();

                    // 检测 C3VK 反爬挑战
                    if (body.contains("C3VK=") && body.length() < 2000) {
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("C3VK=([a-zA-Z0-9]+)").matcher(body);
                        if (m.find()) {
                            cachedC3VK = m.group(1);
                            luoguCookieJar.addCookie("www.luogu.com.cn", "C3VK", cachedC3VK);
                            log.info("[CSRF] 第 {} 次：C3VK 挑战，提取 C3VK={}, 重试...", attempt + 1, cachedC3VK);
                            continue;
                        }
                    }

                    // 从 lentille-context JSON 中提取 csrfToken
                    try {
                        JsonNode root = extractJsonFromHtml(body);
                        String token = root.path("csrfToken").asText(null);
                        if (token != null && !token.isBlank() && !":)".equals(token)) {
                            cachedCsrfToken = token;
                            csrfTokenExpireTime = now + 5 * 60 * 1000;
                            log.info("从页面 JSON 获取洛谷 CSRF Token 成功: {}...", token.substring(0, Math.min(20, token.length())));
                            return token;
                        }
                        log.warn("[CSRF] 第 {} 次：lentille-context 中 csrfToken={}", attempt + 1, token);
                    } catch (Exception e) {
                        log.warn("[CSRF] 第 {} 次：解析 lentille-context 失败: {}", attempt + 1, e.getMessage());
                    }

                    // 兜底：从 meta 标签提取
                    String metaToken = extractCsrfToken(body);
                    if (metaToken != null && !metaToken.isBlank() && !":)".equals(metaToken)) {
                        cachedCsrfToken = metaToken;
                        csrfTokenExpireTime = now + 5 * 60 * 1000;
                        log.info("从 meta 标签获取洛谷 CSRF Token 成功");
                        return metaToken;
                    }

                    log.warn("[CSRF] 第 {} 次：未找到有效 token，响应长度={}", attempt + 1, body.length());
                }
            } catch (IOException e) {
                log.warn("[CSRF] 第 {} 次：网络异常: {}", attempt + 1, e.getMessage());
            }
        }

        throw new OjApiException("无法获取洛谷 CSRF Token（3次尝试均失败），请检查 Cookie 是否有效", "luogu");
    }

    /** 十六进制字符串转字节数组 */
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 从 HTML 中提取 csrf-token
     */
    private String extractCsrfToken(String html) {
        String marker = "csrf-token\" content=\"";
        int idx = html.indexOf(marker);
        if (idx == -1) return null;
        int start = idx + marker.length();
        int end = html.indexOf("\"", start);
        if (end == -1) return null;
        return html.substring(start, end);
    }

    /**
     * 构造洛谷 content-only 请求 Header（用于获取 JSON 数据）
     */
    private Headers buildContentOnlyHeaders() {
        return new Headers.Builder()
                .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                .add("Accept", "application/json, text/plain, */*")
                .add("Accept-Language", "zh-CN,zh;q=0.9")
                .add("Referer", luoguConfig.getBaseUrl() + "/")
                .add("x-luogu-type", "content-only")
                .add("x-requested-with", "XMLHttpRequest")
                .build();
    }

    /**
     * 构造洛谷通用请求 Header（用于页面访问和 CSRF Token 获取）
     */
    private Headers buildHeaders(boolean needCsrf) {
        return new Headers.Builder()
                .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                .add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .add("Accept-Language", "zh-CN,zh;q=0.9")
                .add("Referer", luoguConfig.getBaseUrl() + "/")
                .build();
    }

    /**
     * 构造提交代码时的 Header（需要 CSRF Token）
     */
    private Headers buildSubmitHeaders(String csrfToken) {
        return new Headers.Builder()
                .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                .add("Accept", "application/json, text/plain, */*")
                .add("Accept-Language", "zh-CN,zh;q=0.9")
                .add("Referer", luoguConfig.getBaseUrl() + "/")
                .add("Content-Type", "application/json")
                .add("Origin", luoguConfig.getBaseUrl())
                .add("x-csrf-token", csrfToken)
                .add("x-requested-with", "XMLHttpRequest")
                .build();
    }

    /**
     * 构造洛谷 Cookie 字符串
     */
    private String buildCookie() {
        StringBuilder sb = new StringBuilder();
        if (luoguConfig.getUid() != null && !luoguConfig.getUid().isBlank()) {
            sb.append("_uid=").append(luoguConfig.getUid()).append("; ");
        }
        if (luoguConfig.getClientId() != null && !luoguConfig.getClientId().isBlank()) {
            sb.append("__client_id=").append(luoguConfig.getClientId()).append("; ");
        }
        if (cachedC3VK != null && !cachedC3VK.isBlank()) {
            sb.append("C3VK=").append(cachedC3VK).append("; ");
        }
        return sb.toString();
    }

    /**
     * 统一处理洛谷 API 非成功响应
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
            case 403 -> throw new OjApiException(
                    "洛谷认证失败 [HTTP 403]: " + body, "luogu", 403);
            case 429 -> throw new OjApiException(
                    "洛谷 API 请求频率超限，请稍后再试", "luogu", 429);
            default -> throw new OjApiException(
                    "洛谷 API 调用失败 [HTTP " + code + "]: " + body, "luogu", code);
        }
    }

    /**
     * 格式化内存显示（字节 → KB/MB）
     */
    private String formatMemory(int bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
