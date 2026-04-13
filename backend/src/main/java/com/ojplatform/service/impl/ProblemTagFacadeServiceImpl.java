package com.ojplatform.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojplatform.dto.ProblemTagDTO;
import com.ojplatform.entity.Problem;
import com.ojplatform.mapper.ProblemTagRelationMapper;
import com.ojplatform.service.ProblemTagFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 题目标签统一组装服务实现
 */
@Service
public class ProblemTagFacadeServiceImpl implements ProblemTagFacadeService {

    @Autowired
    private ProblemTagRelationMapper problemTagRelationMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<ProblemTagDTO> getUnifiedTags(Problem problem) {
        if (problem == null) {
            return List.of();
        }

        // 优先使用新关系表
        if (problem.getId() != null) {
            List<ProblemTagDTO> mapped = problemTagRelationMapper.selectUnifiedTagsByProblemId(problem.getId());
            if (mapped != null && !mapped.isEmpty()) {
                normalizeCompatFields(mapped, problem.getOjPlatform());
                return mapped;
            }
        }

        // 兼容旧数据：从 problems.topic_tags 回退解析
        return parseLegacyTopicTags(problem.getTopicTags(), problem.getOjPlatform());
    }

    private void normalizeCompatFields(List<ProblemTagDTO> tags, String ojPlatform) {
        for (ProblemTagDTO tag : tags) {
            if (tag.getLabel() == null || tag.getLabel().isBlank()) {
                tag.setLabel(tag.getSourceName());
            }
            if (tag.getKey() == null || tag.getKey().isBlank()) {
                tag.setKey(buildFallbackKey(tag.getSourceSlug(), tag.getSourceName(), ojPlatform));
            }
            if (tag.getOjPlatform() == null || tag.getOjPlatform().isBlank()) {
                tag.setOjPlatform(ojPlatform);
            }
        }
    }

    private List<ProblemTagDTO> parseLegacyTopicTags(String topicTagsJson, String ojPlatform) {
        if (topicTagsJson == null || topicTagsJson.isBlank()) {
            return List.of();
        }

        try {
            List<Map<String, Object>> raw = objectMapper.readValue(
                    topicTagsJson, new TypeReference<List<Map<String, Object>>>() {});

            List<ProblemTagDTO> result = new ArrayList<>();
            for (Map<String, Object> item : raw) {
                String name = str(item.get("name"));
                String slug = str(item.get("slug"));
                if ((name == null || name.isBlank()) && (slug == null || slug.isBlank())) {
                    continue;
                }

                ProblemTagDTO dto = new ProblemTagDTO();
                dto.setLabel(name != null && !name.isBlank() ? name : slug);
                dto.setSourceName(name);
                dto.setSourceSlug(slug);
                dto.setOjPlatform(ojPlatform);
                dto.setKey(buildFallbackKey(slug, name, ojPlatform));
                dto.setType(inferTagType(name, slug, ojPlatform));
                result.add(dto);
            }
            return result;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private String buildFallbackKey(String slug, String name, String ojPlatform) {
        String normalizedSlug = slug != null ? slug.trim() : "";
        if (!normalizedSlug.isBlank() && !normalizedSlug.matches("\\d+")) {
            return normalizedSlug.toLowerCase(Locale.ROOT);
        }

        String normalizedName = slugifyName(name);
        if (!normalizedName.isBlank()) {
            return normalizedName;
        }

        if (!normalizedSlug.isBlank()) {
            return (ojPlatform != null ? ojPlatform : "unknown") + "-tag-" + normalizedSlug;
        }
        return (ojPlatform != null ? ojPlatform : "unknown") + "-tag";
    }

    private String slugifyName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        String ascii = name.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return ascii;
    }

    private String inferTagType(String name, String slug, String ojPlatform) {
        String merged = ((name != null ? name : "") + " " + (slug != null ? slug : "")).toLowerCase(Locale.ROOT);

        if (merged.contains("array") || merged.contains("hash") || merged.contains("stack")
                || merged.contains("queue") || merged.contains("tree") || merged.contains("graph")
                || merged.contains("链表") || merged.contains("数组") || merged.contains("哈希")
                || merged.contains("栈") || merged.contains("队列") || merged.contains("树")) {
            return "data_structure";
        }

        if (merged.contains("dp") || merged.contains("greedy") || merged.contains("binary")
                || merged.contains("sort") || merged.contains("dfs") || merged.contains("bfs")
                || merged.contains("dynamic-programming") || merged.contains("动态规划")
                || merged.contains("贪心") || merged.contains("二分") || merged.contains("搜索")) {
            return "algorithm";
        }

        if ("luogu".equals(ojPlatform) && slug != null && slug.matches("\\d+")) {
            return "platform_meta";
        }

        return "scenario";
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
