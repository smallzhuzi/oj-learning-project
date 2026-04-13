package com.ojplatform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojplatform.dto.LeetCodeProblemDetail;
import com.ojplatform.entity.PlatformTag;
import com.ojplatform.entity.Problem;
import com.ojplatform.entity.ProblemTagRelation;
import com.ojplatform.entity.Tag;
import com.ojplatform.entity.TagType;
import com.ojplatform.mapper.PlatformTagMapper;
import com.ojplatform.mapper.ProblemMapper;
import com.ojplatform.mapper.ProblemTagRelationMapper;
import com.ojplatform.mapper.TagMapper;
import com.ojplatform.mapper.TagTypeMapper;
import com.ojplatform.service.TagSyncService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签迁移测试。
 *
 * 迁移源：
 * 1. 数据库中的 problems.topic_tags 旧 JSON
 * 2. tag/力扣标签.txt 作为 LeetCode 标签字典
 * 3. tag/洛谷标签.txt 作为洛谷标签字典与类型定义
 */
@SpringBootTest
public class TagMigrationTest {

    private static final Logger log = LoggerFactory.getLogger(TagMigrationTest.class);

    private static final Path LEETCODE_TAG_FILE = Path.of("..", "tag", "力扣标签.txt");
    private static final Path LUOGU_TAG_FILE = Path.of("..", "tag", "洛谷标签.txt");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private TagTypeMapper tagTypeMapper;

    @Autowired
    private PlatformTagMapper platformTagMapper;

    @Autowired
    private ProblemTagRelationMapper problemTagRelationMapper;

    @Autowired
    private TagSyncService tagSyncService;

    @Test
    @Transactional
    public void rebuildAllTagTablesFromLegacyTopicTags() throws Exception {
        log.info("========== 开始清空并重建标签体系 ==========");
        clearNewTagTables();
        migrateAllFromLegacyTopicTags();
    }

    @Test
    public void migrateAllFromLegacyTopicTags() throws Exception {
        migrateLeetCodeFromLegacyTopicTags();
        migrateLuoguFromLegacyTopicTags();
    }

    @Test
    public void migrateLeetCodeFromLegacyTopicTags() throws Exception {
        log.info("========== 开始迁移 LeetCode 旧标签 ==========");

        Map<String, LeetCodeTagMeta> leetCodeTagMetaMap = loadLeetCodeTagMetaMap();
        List<Problem> problems = problemMapper.selectList(new LambdaQueryWrapper<Problem>()
                .eq(Problem::getOjPlatform, "leetcode")
                .isNotNull(Problem::getTopicTags));

        int total = 0;
        int migrated = 0;
        int skippedNoLegacyTags = 0;
        int skippedEmptyResolvedTags = 0;

        for (Problem problem : problems) {
            total++;
            List<Map<String, Object>> legacyTags = parseLegacyTags(problem.getTopicTags());
            if (legacyTags.isEmpty()) {
                skippedNoLegacyTags++;
                continue;
            }

            List<LeetCodeProblemDetail.TopicTag> rawTags = new ArrayList<>();
            for (Map<String, Object> legacyTag : legacyTags) {
                String sourceSlug = str(legacyTag.get("slug"));
                String sourceName = str(legacyTag.get("name"));
                LeetCodeTagMeta meta = sourceSlug == null ? null : leetCodeTagMetaMap.get(sourceSlug);

                String resolvedName = firstNonBlank(
                        meta != null ? meta.nameTranslated() : null,
                        sourceName,
                        meta != null ? meta.name() : null
                );
                String resolvedSlug = firstNonBlank(sourceSlug, meta != null ? meta.slug() : null);

                if ((resolvedName == null || resolvedName.isBlank())
                        && (resolvedSlug == null || resolvedSlug.isBlank())) {
                    continue;
                }

                LeetCodeProblemDetail.TopicTag tag = new LeetCodeProblemDetail.TopicTag();
                tag.setId(meta != null ? meta.id() : resolvedSlug);
                tag.setName(resolvedName);
                tag.setSlug(resolvedSlug);
                rawTags.add(tag);
            }

            if (rawTags.isEmpty()) {
                skippedEmptyResolvedTags++;
                continue;
            }

            tagSyncService.syncProblemTags(problem, rawTags, "leetcode");
            migrated++;

            if (migrated % 200 == 0) {
                log.info("LeetCode 已迁移 {} / {}", migrated, total);
            }
        }

        log.info("LeetCode 标签迁移完成：总题目 {}, 已迁移 {}, 无旧标签 {}, 解析后为空 {}",
                total, migrated, skippedNoLegacyTags, skippedEmptyResolvedTags);
    }

    @Test
    public void migrateLuoguFromLegacyTopicTags() throws Exception {
        log.info("========== 开始迁移洛谷旧标签 ==========");

        Map<String, LuoguTagMeta> luoguTagMetaMap = loadLuoguTagMetaMap();
        List<Problem> problems = problemMapper.selectList(new LambdaQueryWrapper<Problem>()
                .eq(Problem::getOjPlatform, "luogu")
                .isNotNull(Problem::getTopicTags));

        int total = 0;
        int migrated = 0;
        int skippedNoLegacyTags = 0;
        int skippedEmptyResolvedTags = 0;

        for (Problem problem : problems) {
            total++;
            List<Map<String, Object>> legacyTags = parseLegacyTags(problem.getTopicTags());
            if (legacyTags.isEmpty()) {
                skippedNoLegacyTags++;
                continue;
            }

            List<LeetCodeProblemDetail.TopicTag> rawTags = new ArrayList<>();
            for (Map<String, Object> legacyTag : legacyTags) {
                String sourceTagId = firstNonBlank(str(legacyTag.get("id")), str(legacyTag.get("slug")));
                String sourceName = str(legacyTag.get("name"));
                LuoguTagMeta meta = sourceTagId == null ? null : luoguTagMetaMap.get(sourceTagId);
                String resolvedName = firstNonBlank(sourceName, meta != null ? meta.name() : null);

                if ((resolvedName == null || resolvedName.isBlank())
                        && (sourceTagId == null || sourceTagId.isBlank())) {
                    continue;
                }

                LeetCodeProblemDetail.TopicTag tag = new LeetCodeProblemDetail.TopicTag();
                tag.setId(sourceTagId);
                tag.setName(resolvedName);
                tag.setSlug(sourceTagId);
                tag.setType(meta != null ? meta.type() : null);
                rawTags.add(tag);
            }

            if (rawTags.isEmpty()) {
                skippedEmptyResolvedTags++;
                continue;
            }

            tagSyncService.syncProblemTags(problem, rawTags, "luogu");
            migrated++;

            if (migrated % 200 == 0) {
                log.info("洛谷已迁移 {} / {}", migrated, total);
            }
        }

        log.info("洛谷标签迁移完成：总题目 {}, 已迁移 {}, 无旧标签 {}, 解析后为空 {}",
                total, migrated, skippedNoLegacyTags, skippedEmptyResolvedTags);
    }

    @Test
    public void verifyDictionaryFilesReadable() throws Exception {
        Map<String, LeetCodeTagMeta> leetCodeTagMetaMap = loadLeetCodeTagMetaMap();
        Map<String, LuoguTagMeta> luoguTagMetaMap = loadLuoguTagMetaMap();
        log.info("力扣标签字典 {} 条，洛谷标签字典 {} 条", leetCodeTagMetaMap.size(), luoguTagMetaMap.size());
    }

    private void clearNewTagTables() {
        problemTagRelationMapper.delete(new LambdaQueryWrapper<ProblemTagRelation>());
        platformTagMapper.delete(new LambdaQueryWrapper<PlatformTag>());
        tagMapper.delete(new LambdaQueryWrapper<Tag>());
        tagTypeMapper.update(
                null,
                new LambdaUpdateWrapper<TagType>()
                        .set(TagType::getStatus, "active")
        );
        log.info("已清空 problem_tag_relations / platform_tags / tags，保留 tag_types");
    }

    private Map<String, LeetCodeTagMeta> loadLeetCodeTagMetaMap() throws Exception {
        JsonNode root = objectMapper.readTree(Files.readString(LEETCODE_TAG_FILE, StandardCharsets.UTF_8));
        if (!root.isArray()) {
            throw new IllegalStateException("力扣标签文件格式不正确，期望 JSON 数组");
        }

        Map<String, LeetCodeTagMeta> result = new LinkedHashMap<>();
        for (JsonNode item : root) {
            String slug = text(item, "slug");
            if (slug == null || slug.isBlank()) {
                continue;
            }
            result.put(slug, new LeetCodeTagMeta(
                    text(item, "id"),
                    slug,
                    text(item, "name"),
                    text(item, "nameTranslated")
            ));
        }
        return result;
    }

    private Map<String, LuoguTagMeta> loadLuoguTagMetaMap() throws Exception {
        JsonNode root = objectMapper.readTree(Files.readString(LUOGU_TAG_FILE, StandardCharsets.UTF_8));
        JsonNode tags = root.path("tags");
        if (!tags.isArray()) {
            throw new IllegalStateException("洛谷标签文件格式不正确，未找到 tags 数组");
        }

        Map<String, LuoguTagMeta> tagMetaMap = new LinkedHashMap<>();
        for (JsonNode tag : tags) {
            String id = text(tag, "id");
            String name = text(tag, "name");
            Integer type = tag.path("type").isNumber() ? tag.path("type").asInt() : null;
            String parent = text(tag, "parent");
            if (id == null || id.isBlank() || name == null || name.isBlank()) {
                continue;
            }
            tagMetaMap.put(id, new LuoguTagMeta(id, name, type, parent));
        }
        return tagMetaMap;
    }

    private List<Map<String, Object>> parseLegacyTags(String topicTagsJson) {
        if (topicTagsJson == null || topicTagsJson.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(topicTagsJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.warn("旧标签 JSON 解析失败: {}", e.getMessage());
            return List.of();
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

    private String str(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private record LeetCodeTagMeta(String id, String slug, String name, String nameTranslated) {
    }

    private record LuoguTagMeta(String id, String name, Integer type, String parent) {
    }
}
