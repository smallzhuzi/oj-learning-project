package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojplatform.dto.LeetCodeProblemDetail;
import com.ojplatform.entity.PlatformTag;
import com.ojplatform.entity.Problem;
import com.ojplatform.entity.ProblemTagRelation;
import com.ojplatform.entity.Tag;
import com.ojplatform.entity.TagType;
import com.ojplatform.mapper.PlatformTagMapper;
import com.ojplatform.mapper.ProblemTagRelationMapper;
import com.ojplatform.mapper.TagMapper;
import com.ojplatform.mapper.TagTypeMapper;
import com.ojplatform.service.TagSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class TagSyncServiceImpl implements TagSyncService {

    private static final Map<String, String> CANONICAL_TAG_MAP = Map.ofEntries(
            Map.entry("array", "array"),
            Map.entry("数组", "array"),

            Map.entry("backtracking", "backtracking"),
            Map.entry("回溯", "backtracking"),

            Map.entry("biconnected-component", "biconnected-component"),
            Map.entry("双连通分量", "biconnected-component"),

            Map.entry("binary-indexed-tree", "fenwick-tree"),
            Map.entry("树状数组", "fenwick-tree"),
            Map.entry("fenwick-tree", "fenwick-tree"),

            Map.entry("binary-search", "binary-search"),
            Map.entry("二分查找", "binary-search"),
            Map.entry("二分", "binary-search"),

            Map.entry("binary-search-tree", "binary-search-tree"),
            Map.entry("二叉搜索树", "binary-search-tree"),

            Map.entry("binary-tree", "binary-tree"),
            Map.entry("二叉树", "binary-tree"),

            Map.entry("bit-manipulation", "bit-manipulation"),
            Map.entry("位运算", "bit-manipulation"),

            Map.entry("bitmask", "bitmask"),
            Map.entry("位掩码", "bitmask"),

            Map.entry("brainteaser", "brainteaser"),
            Map.entry("脑筋急转弯", "brainteaser"),

            Map.entry("breadth-first-search", "bfs"),
            Map.entry("bfs", "bfs"),
            Map.entry("广度优先搜索", "bfs"),

            Map.entry("bucket-sort", "bucket-sort"),
            Map.entry("桶排序", "bucket-sort"),

            Map.entry("combinatorics", "combinatorics"),
            Map.entry("组合数学", "combinatorics"),

            Map.entry("concurrency", "concurrency"),
            Map.entry("多线程", "concurrency"),

            Map.entry("counting", "counting"),
            Map.entry("计数", "counting"),

            Map.entry("counting-sort", "counting-sort"),
            Map.entry("计数排序", "counting-sort"),

            Map.entry("data-stream", "data-stream"),
            Map.entry("数据流", "data-stream"),

            Map.entry("database", "database"),
            Map.entry("数据库", "database"),

            Map.entry("depth-first-search", "dfs"),
            Map.entry("dfs", "dfs"),
            Map.entry("深度优先搜索", "dfs"),

            Map.entry("design", "design"),
            Map.entry("设计", "design"),

            Map.entry("divide-and-conquer", "divide-and-conquer"),
            Map.entry("分治", "divide-and-conquer"),

            Map.entry("doubly-linked-list", "doubly-linked-list"),
            Map.entry("双向链表", "doubly-linked-list"),

            Map.entry("dynamic-programming", "dp"),
            Map.entry("dp", "dp"),
            Map.entry("动态规划", "dp"),
            Map.entry("动态规划-dp", "dp"),

            Map.entry("enumeration", "enumeration"),
            Map.entry("枚举", "enumeration"),

            Map.entry("eulerian-circuit", "eulerian-circuit"),
            Map.entry("欧拉回路", "eulerian-circuit"),

            Map.entry("game-theory", "game-theory"),
            Map.entry("博弈", "game-theory"),
            Map.entry("博弈论", "game-theory"),

            Map.entry("geometry", "geometry"),
            Map.entry("几何", "geometry"),
            Map.entry("computational-geometry", "geometry"),
            Map.entry("计算几何", "geometry"),

            Map.entry("graph", "graph"),
            Map.entry("图", "graph"),
            Map.entry("图论", "graph"),

            Map.entry("greedy", "greedy"),
            Map.entry("贪心", "greedy"),

            Map.entry("hash-function", "hash-function"),
            Map.entry("哈希函数", "hash-function"),

            Map.entry("hash-table", "hash-table"),
            Map.entry("哈希表", "hash-table"),
            Map.entry("哈希", "hash-table"),

            Map.entry("heap-priority-queue", "heap-priority-queue"),
            Map.entry("heap", "heap-priority-queue"),
            Map.entry("priority-queue", "heap-priority-queue"),
            Map.entry("堆", "heap-priority-queue"),
            Map.entry("堆-优先队列", "heap-priority-queue"),
            Map.entry("堆（优先队列）", "heap-priority-queue"),

            Map.entry("interactive", "interactive"),
            Map.entry("交互", "interactive"),
            Map.entry("交互题", "interactive"),

            Map.entry("iterator", "iterator"),
            Map.entry("迭代器", "iterator"),

            Map.entry("line-sweep", "line-sweep"),
            Map.entry("sweep-line", "line-sweep"),
            Map.entry("扫描线", "line-sweep"),

            Map.entry("linked-list", "linked-list"),
            Map.entry("链表", "linked-list"),

            Map.entry("math", "math"),
            Map.entry("数学", "math"),

            Map.entry("matrix", "matrix"),
            Map.entry("矩阵", "matrix"),
            Map.entry("矩阵运算", "matrix"),

            Map.entry("memoization", "memoization"),
            Map.entry("记忆化", "memoization"),

            Map.entry("merge-sort", "merge-sort"),
            Map.entry("归并排序", "merge-sort"),

            Map.entry("minimum-spanning-tree", "minimum-spanning-tree"),
            Map.entry("最小生成树", "minimum-spanning-tree"),

            Map.entry("monotonic-queue", "monotonic-queue"),
            Map.entry("单调队列", "monotonic-queue"),

            Map.entry("monotonic-stack", "monotonic-stack"),
            Map.entry("单调栈", "monotonic-stack"),

            Map.entry("number-theory", "number-theory"),
            Map.entry("数论", "number-theory"),

            Map.entry("ordered-set", "ordered-set"),
            Map.entry("有序集合", "ordered-set"),

            Map.entry("prefix-sum", "prefix-sum"),
            Map.entry("前缀和", "prefix-sum"),

            Map.entry("probability-and-statistics", "probability-and-statistics"),
            Map.entry("概率与统计", "probability-and-statistics"),

            Map.entry("queue", "queue"),
            Map.entry("队列", "queue"),

            Map.entry("quickselect", "quickselect"),
            Map.entry("快速选择", "quickselect"),

            Map.entry("radix-sort", "radix-sort"),
            Map.entry("基数排序", "radix-sort"),

            Map.entry("randomized", "randomized"),
            Map.entry("随机化", "randomized"),

            Map.entry("recursion", "recursion"),
            Map.entry("递归", "recursion"),

            Map.entry("rejection-sampling", "rejection-sampling"),
            Map.entry("拒绝采样", "rejection-sampling"),

            Map.entry("reservoir-sampling", "reservoir-sampling"),
            Map.entry("水塘抽样", "reservoir-sampling"),

            Map.entry("rolling-hash", "rolling-hash"),
            Map.entry("滚动哈希", "rolling-hash"),

            Map.entry("segment-tree", "segment-tree"),
            Map.entry("线段树", "segment-tree"),

            Map.entry("shell", "shell"),

            Map.entry("shortest-path", "shortest-path"),
            Map.entry("最短路", "shortest-path"),

            Map.entry("simulation", "simulation"),
            Map.entry("模拟", "simulation"),

            Map.entry("sliding-window", "sliding-window"),
            Map.entry("滑动窗口", "sliding-window"),

            Map.entry("sort", "sorting"),
            Map.entry("sorting", "sorting"),
            Map.entry("排序", "sorting"),

            Map.entry("stack", "stack"),
            Map.entry("栈", "stack"),

            Map.entry("string", "string"),
            Map.entry("字符串", "string"),

            Map.entry("string-matching", "string-matching"),
            Map.entry("字符串匹配", "string-matching"),

            Map.entry("strongly-connected-component", "strongly-connected-component"),
            Map.entry("强连通分量", "strongly-connected-component"),

            Map.entry("suffix-array", "suffix-array"),
            Map.entry("后缀数组", "suffix-array"),

            Map.entry("topological-sort", "topological-sort"),
            Map.entry("拓扑排序", "topological-sort"),

            Map.entry("tree", "tree"),
            Map.entry("树", "tree"),

            Map.entry("trie", "trie"),
            Map.entry("字典树", "trie"),

            Map.entry("two-pointers", "two-pointers"),
            Map.entry("双指针", "two-pointers"),

            Map.entry("union-find", "union-find"),
            Map.entry("并查集", "union-find"),

            Map.entry("language-intro", "language-intro"),
            Map.entry("语言入门", "language-intro"),

            Map.entry("usaco", "usaco"),
            Map.entry("noi", "noi"),
            Map.entry("各省省选", "provincial-selection"),
            Map.entry("集训队互测", "training-contest"),
            Map.entry("poi（波兰）", "poi"),
            Map.entry("poi-波兰", "poi"),
            Map.entry("poi", "poi"),
            Map.entry("福建省历届夏令营", "fujian-summer-camp")
    );

    private static final Map<String, String> CANONICAL_DISPLAY_MAP = Map.ofEntries(
            Map.entry("array", "数组"),
            Map.entry("backtracking", "回溯"),
            Map.entry("biconnected-component", "双连通分量"),
            Map.entry("fenwick-tree", "树状数组"),
            Map.entry("binary-search", "二分查找"),
            Map.entry("binary-search-tree", "二叉搜索树"),
            Map.entry("binary-tree", "二叉树"),
            Map.entry("bit-manipulation", "位运算"),
            Map.entry("bitmask", "位掩码"),
            Map.entry("brainteaser", "脑筋急转弯"),
            Map.entry("bfs", "广度优先搜索"),
            Map.entry("bucket-sort", "桶排序"),
            Map.entry("combinatorics", "组合数学"),
            Map.entry("concurrency", "多线程"),
            Map.entry("counting", "计数"),
            Map.entry("counting-sort", "计数排序"),
            Map.entry("data-stream", "数据流"),
            Map.entry("database", "数据库"),
            Map.entry("dfs", "深度优先搜索"),
            Map.entry("design", "设计"),
            Map.entry("divide-and-conquer", "分治"),
            Map.entry("doubly-linked-list", "双向链表"),
            Map.entry("dp", "动态规划"),
            Map.entry("enumeration", "枚举"),
            Map.entry("eulerian-circuit", "欧拉回路"),
            Map.entry("game-theory", "博弈论"),
            Map.entry("geometry", "几何"),
            Map.entry("graph", "图论"),
            Map.entry("greedy", "贪心"),
            Map.entry("hash-function", "哈希函数"),
            Map.entry("hash-table", "哈希表"),
            Map.entry("heap-priority-queue", "堆（优先队列）"),
            Map.entry("interactive", "交互题"),
            Map.entry("iterator", "迭代器"),
            Map.entry("line-sweep", "扫描线"),
            Map.entry("linked-list", "链表"),
            Map.entry("math", "数学"),
            Map.entry("matrix", "矩阵"),
            Map.entry("memoization", "记忆化"),
            Map.entry("merge-sort", "归并排序"),
            Map.entry("minimum-spanning-tree", "最小生成树"),
            Map.entry("monotonic-queue", "单调队列"),
            Map.entry("monotonic-stack", "单调栈"),
            Map.entry("number-theory", "数论"),
            Map.entry("ordered-set", "有序集合"),
            Map.entry("prefix-sum", "前缀和"),
            Map.entry("probability-and-statistics", "概率与统计"),
            Map.entry("queue", "队列"),
            Map.entry("quickselect", "快速选择"),
            Map.entry("radix-sort", "基数排序"),
            Map.entry("randomized", "随机化"),
            Map.entry("recursion", "递归"),
            Map.entry("rejection-sampling", "拒绝采样"),
            Map.entry("reservoir-sampling", "水塘抽样"),
            Map.entry("rolling-hash", "滚动哈希"),
            Map.entry("segment-tree", "线段树"),
            Map.entry("shell", "Shell"),
            Map.entry("shortest-path", "最短路"),
            Map.entry("simulation", "模拟"),
            Map.entry("sliding-window", "滑动窗口"),
            Map.entry("sorting", "排序"),
            Map.entry("stack", "栈"),
            Map.entry("string", "字符串"),
            Map.entry("string-matching", "字符串匹配"),
            Map.entry("strongly-connected-component", "强连通分量"),
            Map.entry("suffix-array", "后缀数组"),
            Map.entry("topological-sort", "拓扑排序"),
            Map.entry("tree", "树"),
            Map.entry("trie", "字典树"),
            Map.entry("two-pointers", "双指针"),
            Map.entry("union-find", "并查集"),
            Map.entry("language-intro", "语言入门"),
            Map.entry("usaco", "USACO"),
            Map.entry("noi", "NOI"),
            Map.entry("provincial-selection", "各省省选"),
            Map.entry("training-contest", "集训队互测"),
            Map.entry("poi", "POI"),
            Map.entry("fujian-summer-camp", "福建省历届夏令营")
    );

    private static final Map<String, String> CANONICAL_TYPE_MAP = Map.ofEntries(
            Map.entry("array", "knowledge"),
            Map.entry("backtracking", "knowledge"),
            Map.entry("biconnected-component", "knowledge"),
            Map.entry("fenwick-tree", "knowledge"),
            Map.entry("binary-search", "knowledge"),
            Map.entry("binary-search-tree", "knowledge"),
            Map.entry("binary-tree", "knowledge"),
            Map.entry("bit-manipulation", "knowledge"),
            Map.entry("bitmask", "knowledge"),
            Map.entry("brainteaser", "knowledge"),
            Map.entry("bfs", "knowledge"),
            Map.entry("bucket-sort", "knowledge"),
            Map.entry("combinatorics", "knowledge"),
            Map.entry("concurrency", "knowledge"),
            Map.entry("counting", "knowledge"),
            Map.entry("counting-sort", "knowledge"),
            Map.entry("data-stream", "knowledge"),
            Map.entry("database", "knowledge"),
            Map.entry("dfs", "knowledge"),
            Map.entry("design", "knowledge"),
            Map.entry("divide-and-conquer", "knowledge"),
            Map.entry("doubly-linked-list", "knowledge"),
            Map.entry("dp", "knowledge"),
            Map.entry("enumeration", "knowledge"),
            Map.entry("eulerian-circuit", "knowledge"),
            Map.entry("game-theory", "knowledge"),
            Map.entry("geometry", "knowledge"),
            Map.entry("graph", "knowledge"),
            Map.entry("greedy", "knowledge"),
            Map.entry("hash-function", "knowledge"),
            Map.entry("hash-table", "knowledge"),
            Map.entry("heap-priority-queue", "knowledge"),
            Map.entry("interactive", "special"),
            Map.entry("iterator", "knowledge"),
            Map.entry("line-sweep", "knowledge"),
            Map.entry("linked-list", "knowledge"),
            Map.entry("math", "knowledge"),
            Map.entry("matrix", "knowledge"),
            Map.entry("memoization", "knowledge"),
            Map.entry("merge-sort", "knowledge"),
            Map.entry("minimum-spanning-tree", "knowledge"),
            Map.entry("monotonic-queue", "knowledge"),
            Map.entry("monotonic-stack", "knowledge"),
            Map.entry("number-theory", "knowledge"),
            Map.entry("ordered-set", "knowledge"),
            Map.entry("prefix-sum", "knowledge"),
            Map.entry("probability-and-statistics", "knowledge"),
            Map.entry("queue", "knowledge"),
            Map.entry("quickselect", "knowledge"),
            Map.entry("radix-sort", "knowledge"),
            Map.entry("randomized", "knowledge"),
            Map.entry("recursion", "knowledge"),
            Map.entry("rejection-sampling", "knowledge"),
            Map.entry("reservoir-sampling", "knowledge"),
            Map.entry("rolling-hash", "knowledge"),
            Map.entry("segment-tree", "knowledge"),
            Map.entry("shell", "knowledge"),
            Map.entry("shortest-path", "knowledge"),
            Map.entry("simulation", "knowledge"),
            Map.entry("sliding-window", "knowledge"),
            Map.entry("sorting", "knowledge"),
            Map.entry("stack", "knowledge"),
            Map.entry("string", "knowledge"),
            Map.entry("string-matching", "knowledge"),
            Map.entry("strongly-connected-component", "knowledge"),
            Map.entry("suffix-array", "knowledge"),
            Map.entry("topological-sort", "knowledge"),
            Map.entry("tree", "knowledge"),
            Map.entry("trie", "knowledge"),
            Map.entry("two-pointers", "knowledge"),
            Map.entry("union-find", "knowledge"),
            Map.entry("language-intro", "knowledge"),
            Map.entry("usaco", "source"),
            Map.entry("noi", "source"),
            Map.entry("provincial-selection", "source"),
            Map.entry("training-contest", "source"),
            Map.entry("poi", "source"),
            Map.entry("fujian-summer-camp", "source")
    );

    @Autowired
    private TagTypeMapper tagTypeMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PlatformTagMapper platformTagMapper;

    @Autowired
    private ProblemTagRelationMapper problemTagRelationMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public void syncProblemTags(Problem problem, List<LeetCodeProblemDetail.TopicTag> rawTags, String ojPlatform) {
        if (problem == null || problem.getId() == null || rawTags == null || rawTags.isEmpty()) {
            return;
        }

        problemTagRelationMapper.delete(new LambdaQueryWrapper<ProblemTagRelation>()
                .eq(ProblemTagRelation::getProblemId, problem.getId()));

        for (LeetCodeProblemDetail.TopicTag rawTag : rawTags) {
            if (rawTag == null) {
                continue;
            }

            String sourceTagId = trim(rawTag.getId());
            String sourceName = trim(rawTag.getName());
            String sourceSlug = trim(rawTag.getSlug());
            if (isBlank(sourceName) && isBlank(sourceSlug)) {
                continue;
            }

            CanonicalTag canonicalTag = resolveCanonicalTag(rawTag, ojPlatform);
            TagType tagType = getOrCreateTagType(canonicalTag.typeKey());
            Tag tag = getOrCreateTag(tagType.getId(), canonicalTag.key(), canonicalTag.displayName(), canonicalTag.aliases());
            PlatformTag platformTag = getOrCreatePlatformTag(
                    ojPlatform,
                    firstNonBlank(sourceTagId, sourceSlug),
                    sourceSlug,
                    sourceName,
                    canonicalTag.key(),
                    tagType.getId(),
                    tag.getId()
            );

            ProblemTagRelation relation = new ProblemTagRelation();
            relation.setProblemId(problem.getId());
            relation.setTagId(tag.getId());
            relation.setPlatformTagId(platformTag.getId());
            problemTagRelationMapper.insert(relation);
        }
    }

    private CanonicalTag resolveCanonicalTag(LeetCodeProblemDetail.TopicTag rawTag, String ojPlatform) {
        String sourceName = trim(rawTag.getName());
        String sourceSlug = trim(rawTag.getSlug());
        String canonicalKey = findCanonicalKey(sourceName, sourceSlug);
        if (canonicalKey == null) {
            canonicalKey = buildFallbackKey(sourceName, sourceSlug, ojPlatform);
        }

        String displayName = CANONICAL_DISPLAY_MAP.get(canonicalKey);
        if (isBlank(displayName)) {
            displayName = !isBlank(sourceName) ? sourceName : canonicalKey;
        }

        String typeKey = CANONICAL_TYPE_MAP.get(canonicalKey);
        if (isBlank(typeKey)) {
            typeKey = inferTagType(rawTag, ojPlatform, canonicalKey);
        }

        LinkedHashSet<String> aliases = new LinkedHashSet<>();
        addAlias(aliases, displayName);
        addAlias(aliases, sourceName);
        addAlias(aliases, sourceSlug);
        addAlias(aliases, canonicalKey);

        return new CanonicalTag(canonicalKey, displayName, typeKey, List.copyOf(aliases));
    }

    public String debugResolveCanonicalKey(String sourceName, String sourceSlug, String ojPlatform) {
        LeetCodeProblemDetail.TopicTag rawTag = new LeetCodeProblemDetail.TopicTag();
        rawTag.setName(sourceName);
        rawTag.setSlug(sourceSlug);
        return resolveCanonicalTag(rawTag, ojPlatform).key();
    }

    private String findCanonicalKey(String sourceName, String sourceSlug) {
        String slugKey = normalizeLookupKey(sourceSlug);
        if (!isBlank(slugKey) && CANONICAL_TAG_MAP.containsKey(slugKey)) {
            return CANONICAL_TAG_MAP.get(slugKey);
        }

        String nameKey = normalizeLookupKey(sourceName);
        if (!isBlank(nameKey) && CANONICAL_TAG_MAP.containsKey(nameKey)) {
            return CANONICAL_TAG_MAP.get(nameKey);
        }

        return null;
    }

    private TagType getOrCreateTagType(String typeKey) {
        TagType existing = tagTypeMapper.selectOne(new LambdaQueryWrapper<TagType>()
                .eq(TagType::getTypeKey, typeKey)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }

        TagType created = new TagType();
        created.setTypeKey(typeKey);
        created.setTypeName(defaultTypeName(typeKey));
        created.setDescription(defaultTypeName(typeKey) + "标签");
        created.setSortOrder(defaultTypeSort(typeKey));
        created.setStatus("active");
        tagTypeMapper.insert(created);
        return created;
    }

    private Tag getOrCreateTag(Long tagTypeId, String tagKey, String displayName, List<String> aliases) {
        Tag existing = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getTagTypeId, tagTypeId)
                .eq(Tag::getTagKey, tagKey)
                .last("LIMIT 1"));
        if (existing != null) {
            boolean updated = false;
            if (isBlank(existing.getDisplayName()) && !isBlank(displayName)) {
                existing.setDisplayName(displayName);
                updated = true;
            }

            String mergedAliases = mergeAliases(existing.getAliasNames(), aliases);
            if (!Objects.equals(existing.getAliasNames(), mergedAliases)) {
                existing.setAliasNames(mergedAliases);
                updated = true;
            }

            if (updated) {
                tagMapper.updateById(existing);
            }
            return existing;
        }

        Tag created = new Tag();
        created.setTagTypeId(tagTypeId);
        created.setTagKey(tagKey);
        created.setDisplayName(displayName);
        created.setStatus("active");
        created.setSortOrder(0);
        created.setAliasNames(toJson(aliases));
        tagMapper.insert(created);
        return created;
    }

    private PlatformTag getOrCreatePlatformTag(String ojPlatform, String sourceTagId, String sourceSlug,
                                               String sourceName, String normalizedKey, Long tagTypeId, Long tagId) {
        PlatformTag existing = platformTagMapper.selectOne(new LambdaQueryWrapper<PlatformTag>()
                .eq(PlatformTag::getOjPlatform, ojPlatform)
                .eq(PlatformTag::getSourceTagId, sourceTagId)
                .eq(PlatformTag::getSourceSlug, sourceSlug)
                .eq(PlatformTag::getSourceName, sourceName)
                .last("LIMIT 1"));
        if (existing != null) {
            platformTagMapper.update(null, new LambdaUpdateWrapper<PlatformTag>()
                    .eq(PlatformTag::getId, existing.getId())
                    .set(PlatformTag::getNormalizedKey, normalizedKey)
                    .set(PlatformTag::getTagTypeId, tagTypeId)
                    .set(PlatformTag::getTagId, tagId));
            existing.setNormalizedKey(normalizedKey);
            existing.setTagTypeId(tagTypeId);
            existing.setTagId(tagId);
            return existing;
        }

        PlatformTag created = new PlatformTag();
        created.setOjPlatform(ojPlatform);
        created.setSourceTagId(sourceTagId);
        created.setSourceSlug(sourceSlug);
        created.setSourceName(sourceName);
        created.setNormalizedKey(normalizedKey);
        created.setTagTypeId(tagTypeId);
        created.setTagId(tagId);
        created.setMetadata(toJson(Map.of(
                "sourceSlug", Objects.toString(sourceSlug, ""),
                "sourceName", Objects.toString(sourceName, "")
        )));
        platformTagMapper.insert(created);
        return created;
    }

    private String inferTagType(LeetCodeProblemDetail.TopicTag rawTag, String ojPlatform, String canonicalKey) {
        String name = trim(rawTag.getName());
        String slug = trim(rawTag.getSlug());
        if (!isBlank(canonicalKey) && CANONICAL_TYPE_MAP.containsKey(canonicalKey)) {
            return CANONICAL_TYPE_MAP.get(canonicalKey);
        }

        if ("luogu".equalsIgnoreCase(ojPlatform)) {
            String luoguTypeKey = mapLuoguTypeKey(rawTag.getType());
            if (!isBlank(luoguTypeKey)) {
                return luoguTypeKey;
            }
        }

        if (looksLikeYear(name) || looksLikeYear(slug)) {
            return "year";
        }

        return "knowledge";
    }

    private String mapLuoguTypeKey(Integer rawType) {
        if (rawType == null) {
            return null;
        }
        return switch (rawType) {
            case 1 -> "region";
            case 2 -> "knowledge";
            case 3 -> "source";
            case 4 -> "year";
            case 5 -> "special";
            case 6 -> "category";
            default -> null;
        };
    }

    private String buildFallbackKey(String name, String slug, String ojPlatform) {
        String normalizedSlug = trim(slug);
        if (!isBlank(normalizedSlug) && !normalizedSlug.matches("\\d+")) {
            return normalizedSlug.toLowerCase(Locale.ROOT);
        }

        String normalizedName = slugifyName(name);
        if (!isBlank(normalizedName)) {
            return normalizedName;
        }

        if (!isBlank(normalizedSlug)) {
            return ojPlatform + "-tag-" + normalizedSlug;
        }
        return ojPlatform + "-tag";
    }

    private String normalizeLookupKey(String value) {
        if (isBlank(value)) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace('（', '(')
                .replace('）', ')')
                .replaceAll("[\\s_/]+", "-")
                .replaceAll("[()]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String slugifyName(String name) {
        if (isBlank(name)) {
            return "";
        }
        return name.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    private String mergeAliases(String existingAliasJson, List<String> incomingAliases) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (!isBlank(existingAliasJson)) {
            try {
                List<String> existing = objectMapper.readValue(existingAliasJson, new TypeReference<List<String>>() {});
                for (String alias : existing) {
                    addAlias(merged, alias);
                }
            } catch (Exception ignored) {
            }
        }
        if (incomingAliases != null) {
            for (String alias : incomingAliases) {
                addAlias(merged, alias);
            }
        }
        return toJson(new ArrayList<>(merged));
    }

    private void addAlias(Set<String> aliases, String alias) {
        if (!isBlank(alias)) {
            aliases.add(alias.trim());
        }
    }

    private String defaultTypeName(String typeKey) {
        return switch (typeKey) {
            case "knowledge" -> "知识点";
            case "source" -> "来源";
            case "region" -> "地区";
            case "year" -> "时间";
            case "special" -> "特殊题目";
            case "category" -> "分类";
            case "platform_meta" -> "平台元信息";
            default -> "知识点";
        };
    }

    private int defaultTypeSort(String typeKey) {
        return switch (typeKey) {
            case "knowledge" -> 10;
            case "source" -> 20;
            case "region" -> 30;
            case "year" -> 40;
            case "special" -> 50;
            case "category" -> 60;
            default -> 70;
        };
    }

    private boolean looksLikeYear(String value) {
        if (isBlank(value)) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.matches("19\\d{2}|20\\d{2}");
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private record CanonicalTag(String key, String displayName, String typeKey, List<String> aliases) {
    }
}
