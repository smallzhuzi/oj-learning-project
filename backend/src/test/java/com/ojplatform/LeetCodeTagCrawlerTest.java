package com.ojplatform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ojplatform.config.LeetCodeConfig;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LeetCode 标签抓取测试。
 *
 * 作用：
 * 1. 从 LeetCode CN 的题单接口抓取所有题目的 topicTags
 * 2. 按 slug 去重
 * 3. 输出到 tag/力扣标签.txt
 *
 * 用法：
 * cd backend
 * mvn test -Dtest=LeetCodeTagCrawlerTest#crawlAllTags
 */
@SpringBootTest
public class LeetCodeTagCrawlerTest {

    private static final Logger log = LoggerFactory.getLogger(LeetCodeTagCrawlerTest.class);

    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final int PAGE_SIZE = 100;
    private static final long REQUEST_SLEEP_MS = 500L;
    private static final Path OUTPUT_FILE = Path.of("..", "tag", "力扣标签.txt");

    private static final String TAG_QUERY = """
            query problemsetQuestionListV2($categorySlug: String, $limit: Int, $skip: Int, $filters: QuestionFilterInput) {
              problemsetQuestionListV2(
                categorySlug: $categorySlug
                limit: $limit
                skip: $skip
                filters: $filters
              ) {
                hasMore
                totalLength
                questions {
                  titleSlug
                  title
                  translatedTitle
                  topicTags {
                    id
                    name
                    slug
                    nameTranslated
                  }
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

    @Test
    public void crawlAllTags() throws Exception {
        log.info("========== 开始抓取 LeetCode 全量标签 ==========");

        Map<String, ObjectNode> uniqueTags = new LinkedHashMap<>();
        int skip = 0;
        int totalQuestions = 0;

        while (true) {
            JsonNode pageData = fetchTagPage(skip, PAGE_SIZE);
            JsonNode listNode = pageData.path("problemsetQuestionListV2");
            JsonNode questions = listNode.path("questions");
            boolean hasMore = listNode.path("hasMore").asBoolean(false);

            if (!questions.isArray() || questions.isEmpty()) {
                log.info("题单返回空页，结束抓取，skip={}", skip);
                break;
            }

            totalQuestions += questions.size();
            for (JsonNode question : questions) {
                JsonNode topicTags = question.path("topicTags");
                if (!topicTags.isArray()) {
                    continue;
                }

                for (JsonNode tag : topicTags) {
                    String slug = text(tag, "slug");
                    String id = text(tag, "id");
                    String key = firstNonBlank(slug, id);
                    if (key == null || key.isBlank()) {
                        continue;
                    }

                    ObjectNode existing = uniqueTags.get(key);
                    if (existing == null) {
                        ObjectNode node = objectMapper.createObjectNode();
                        node.put("id", id);
                        node.put("slug", slug);
                        node.put("name", text(tag, "name"));
                        node.put("nameTranslated", text(tag, "nameTranslated"));
                        node.put("count", 1);
                        uniqueTags.put(key, node);
                    } else {
                        existing.put("count", existing.path("count").asInt(0) + 1);
                        fillIfBlank(existing, "id", id);
                        fillIfBlank(existing, "slug", slug);
                        fillIfBlank(existing, "name", text(tag, "name"));
                        fillIfBlank(existing, "nameTranslated", text(tag, "nameTranslated"));
                    }
                }
            }

            log.info("已抓取题目 {} 道，累计唯一标签 {} 个", totalQuestions, uniqueTags.size());

            if (!hasMore) {
                break;
            }

            skip += PAGE_SIZE;
            Thread.sleep(REQUEST_SLEEP_MS);
        }

        ArrayNode output = objectMapper.createArrayNode();
        uniqueTags.values().stream()
                .sorted(Comparator.comparing(node -> node.path("slug").asText("")))
                .forEach(output::add);

        Files.createDirectories(OUTPUT_FILE.getParent());
        Files.writeString(
                OUTPUT_FILE,
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(output),
                StandardCharsets.UTF_8
        );

        log.info("LeetCode 标签抓取完成：唯一标签 {} 个，已写入 {}", output.size(), OUTPUT_FILE.toAbsolutePath());
    }

    private JsonNode fetchTagPage(int skip, int limit) throws Exception {
        ObjectNode variables = objectMapper.createObjectNode();
        variables.put("categorySlug", "all-code-essentials");
        variables.put("skip", skip);
        variables.put("limit", limit);
        variables.putNull("filters");

        ObjectNode body = objectMapper.createObjectNode();
        body.put("query", TAG_QUERY);
        body.set("variables", variables);
        body.put("operationName", "problemsetQuestionListV2");

        Request request = new Request.Builder()
                .url(leetCodeConfig.getBaseUrl() + "/graphql/")
                .headers(buildHeaders())
                .post(RequestBody.create(objectMapper.writeValueAsString(body), JSON_MEDIA_TYPE))
                .build();

        try (Response response = leetCodeHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String bodyText = response.body() != null ? response.body().string() : "";
                throw new IllegalStateException("LeetCode 请求失败 [HTTP " + response.code() + "]: " + bodyText);
            }

            String responseBody = response.body() != null ? response.body().string() : "{}";
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode errors = root.path("errors");
            if (errors.isArray() && !errors.isEmpty()) {
                throw new IllegalStateException("LeetCode GraphQL 返回错误: " + errors.toString());
            }
            return root.path("data");
        }
    }

    private Headers buildHeaders() {
        return new Headers.Builder()
                .add("Cookie", leetCodeConfig.getSession())
                .add("x-csrftoken", leetCodeConfig.getCsrfToken())
                .add("Referer", leetCodeConfig.getBaseUrl() + "/problemset/")
                .add("Content-Type", "application/json")
                .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                .add("Origin", leetCodeConfig.getBaseUrl())
                .build();
    }

    private void fillIfBlank(ObjectNode node, String fieldName, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (node.path(fieldName).asText("").isBlank()) {
            node.put(fieldName, value);
        }
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null ? null : text.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
