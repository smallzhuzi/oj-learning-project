package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojplatform.dto.CreateProblemSetDTO;
import com.ojplatform.dto.ProblemSetItemDetailDTO;
import com.ojplatform.dto.QuickGenerateDTO;
import com.ojplatform.entity.Problem;
import com.ojplatform.entity.ProblemSet;
import com.ojplatform.entity.ProblemSetItem;
import com.ojplatform.mapper.ProblemSetItemMapper;
import com.ojplatform.mapper.ProblemSetMapper;
import com.ojplatform.mapper.ProblemTagRelationMapper;
import com.ojplatform.service.ProblemService;
import com.ojplatform.service.ProblemSetService;
import com.ojplatform.service.SubmissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题单相关业务实现。
 */
@Service
public class ProblemSetServiceImpl extends ServiceImpl<ProblemSetMapper, ProblemSet> implements ProblemSetService {

    private static final Logger log = LoggerFactory.getLogger(ProblemSetServiceImpl.class);

    private static final Map<String, Integer> BEGINNER_DIST = Map.of("Easy", 70, "Medium", 30, "Hard", 0);
    private static final Map<String, Integer> INTERMEDIATE_DIST = Map.of("Easy", 20, "Medium", 60, "Hard", 20);
    private static final Map<String, Integer> ADVANCED_DIST = Map.of("Easy", 0, "Medium", 40, "Hard", 60);

    @Autowired
    private ProblemSetItemMapper problemSetItemMapper;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProblemTagRelationMapper problemTagRelationMapper;

/**
 * 创建题单并写入题目条目。
 */
    @Override
    @Transactional
    public ProblemSet createProblemSet(CreateProblemSetDTO dto) {
        ProblemSet ps = new ProblemSet();
        ps.setUserId(dto.getUserId());
        ps.setTitle(dto.getTitle());
        ps.setDescription(dto.getDescription());
        ps.setSourceType("manual");
        ps.setDifficultyLevel(dto.getDifficultyLevel());
        ps.setOjPlatform(dto.getOjPlatform());
        ps.setVisibility("private");
        ps.setStatus("draft");
        ps.setProblemCount(0);
        ps.setTotalScore(0);
        baseMapper.insert(ps);

        if (dto.getProblems() != null && !dto.getProblems().isEmpty()) {
            int seq = 1;
            int totalScore = 0;
            for (CreateProblemSetDTO.ProblemItem item : dto.getProblems()) {
                Problem problem = problemService.getBySlug(item.getSlug(), dto.getOjPlatform());
                if (problem == null) {
                    log.warn("题目不存在，跳过：{}", item.getSlug());
                    continue;
                }
                ProblemSetItem psi = new ProblemSetItem();
                psi.setSetId(ps.getId());
                psi.setProblemId(problem.getId());
                psi.setSeqOrder(seq++);
                psi.setScore(item.getScore() != null ? item.getScore() : 100);
                problemSetItemMapper.insert(psi);
                totalScore += psi.getScore();
            }
            ps.setProblemCount(seq - 1);
            ps.setTotalScore(totalScore);
            baseMapper.updateById(ps);
        }

        log.info("创建题单：id={}, title={}, 题目数={}", ps.getId(), ps.getTitle(), ps.getProblemCount());
        return ps;
    }

/**
 * 根据条件快速生成题单。
 */
    @Override
    @Transactional
    public ProblemSet quickGenerate(QuickGenerateDTO dto) {
        Map<String, Integer> distribution = getDistribution(dto);
        int totalCount = dto.getCount();

        int easyCount = (int) Math.round(totalCount * distribution.getOrDefault("Easy", 0) / 100.0);
        int hardCount = (int) Math.round(totalCount * distribution.getOrDefault("Hard", 0) / 100.0);
        int mediumCount = totalCount - easyCount - hardCount;

        Set<String> solvedSlugs = new HashSet<>();
        if (Boolean.TRUE.equals(dto.getExcludeSolved())) {
            Map<String, String> statusMap = submissionService.getUserStatusMap(dto.getUserId());
            solvedSlugs = statusMap.entrySet().stream()
                    .filter(e -> "accepted".equals(e.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }

        List<Problem> selectedProblems = new ArrayList<>();
        if (easyCount > 0) {
            selectedProblems.addAll(randomPickProblems("Easy", easyCount, dto.getOjPlatform(), dto.getTags(), solvedSlugs));
        }
        if (mediumCount > 0) {
            selectedProblems.addAll(randomPickProblems("Medium", mediumCount, dto.getOjPlatform(), dto.getTags(), solvedSlugs));
        }
        if (hardCount > 0) {
            selectedProblems.addAll(randomPickProblems("Hard", hardCount, dto.getOjPlatform(), dto.getTags(), solvedSlugs));
        }

        if (selectedProblems.isEmpty()) {
            throw new RuntimeException("没有符合条件的题目，请调整筛选条件");
        }

        Map<String, Integer> diffOrder = Map.of("Easy", 1, "Medium", 2, "Hard", 3);
        selectedProblems.sort(Comparator.comparingInt(p -> diffOrder.getOrDefault(p.getDifficulty(), 99)));

        String title = dto.getTitle();
        if (title != null) {
            title = title.trim();
        }
        if (title == null || title.isBlank()) {
            title = "快速组题 - " + dto.getDifficultyLevel() + " (" + selectedProblems.size() + "题)";
        }

        ProblemSet ps = new ProblemSet();
        ps.setUserId(dto.getUserId());
        ps.setTitle(title);
        ps.setSourceType("quick");
        ps.setDifficultyLevel(dto.getDifficultyLevel());
        ps.setOjPlatform(dto.getOjPlatform());
        ps.setVisibility("private");
        ps.setStatus("published");
        ps.setProblemCount(selectedProblems.size());

        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            try {
                ps.setTags(objectMapper.writeValueAsString(dto.getTags()));
            } catch (Exception e) {
                log.warn("序列化标签失败", e);
            }
        }

        int totalScore = selectedProblems.size() * 100;
        ps.setTotalScore(totalScore);
        baseMapper.insert(ps);

        int seq = 1;
        for (Problem p : selectedProblems) {
            ProblemSetItem psi = new ProblemSetItem();
            psi.setSetId(ps.getId());
            psi.setProblemId(p.getId());
            psi.setSeqOrder(seq++);
            psi.setScore(100);
            problemSetItemMapper.insert(psi);
        }

        log.info("快速组题完成：id={}, 难度={}, 题目数={}", ps.getId(), dto.getDifficultyLevel(), selectedProblems.size());
        return ps;
    }

/**
 * 分页查询用户自己的题单。
 */
    @Override
    public IPage<ProblemSet> getUserProblemSets(Long userId, int pageNum, int pageSize) {
        return baseMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProblemSet>()
                        .eq(ProblemSet::getUserId, userId)
                        .ne(ProblemSet::getStatus, "archived")
                        .orderByDesc(ProblemSet::getUpdatedAt)
        );
    }

/**
 * 分页查询公开题单。
 */
    @Override
    public IPage<ProblemSet> getPublicProblemSets(int pageNum, int pageSize) {
        return baseMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProblemSet>()
                        .eq(ProblemSet::getVisibility, "public")
                        .eq(ProblemSet::getStatus, "published")
                        .orderByDesc(ProblemSet::getUpdatedAt)
        );
    }

/**
 * 查询题单中的题目详情。
 */
    @Override
    public List<ProblemSetItemDetailDTO> getProblemSetItems(Long setId) {
        return problemSetItemMapper.selectItemsWithDetail(setId);
    }

/**
 * 向题单追加题目。
 */
    @Override
    @Transactional
    public void addProblemToSet(Long setId, String problemSlug, Integer score, Long userId) {
        ProblemSet ps = baseMapper.selectById(setId);
        if (ps == null) {
            throw new RuntimeException("题单不存在");
        }
        if (!ps.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此题单");
        }

        Problem problem = problemService.getBySlug(problemSlug, ps.getOjPlatform());
        if (problem == null) {
            throw new RuntimeException("题目不存在：" + problemSlug);
        }

        Long count = problemSetItemMapper.selectCount(
                new LambdaQueryWrapper<ProblemSetItem>()
                        .eq(ProblemSetItem::getSetId, setId)
                        .eq(ProblemSetItem::getProblemId, problem.getId())
        );
        if (count > 0) {
            throw new RuntimeException("该题目已在题单中");
        }

        int maxSeq = ps.getProblemCount();

        ProblemSetItem psi = new ProblemSetItem();
        psi.setSetId(setId);
        psi.setProblemId(problem.getId());
        psi.setSeqOrder(maxSeq + 1);
        psi.setScore(score != null ? score : 100);
        problemSetItemMapper.insert(psi);

        ps.setProblemCount(maxSeq + 1);
        ps.setTotalScore(ps.getTotalScore() + psi.getScore());
        baseMapper.updateById(ps);
    }

/**
 * 从题单移除题目。
 */
    @Override
    @Transactional
    public void removeProblemFromSet(Long setId, Long itemId, Long userId) {
        ProblemSet ps = baseMapper.selectById(setId);
        if (ps == null) {
            throw new RuntimeException("题单不存在");
        }
        if (!ps.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此题单");
        }

        ProblemSetItem item = problemSetItemMapper.selectById(itemId);
        if (item == null || !item.getSetId().equals(setId)) {
            throw new RuntimeException("题目关联不存在");
        }

        problemSetItemMapper.deleteById(itemId);

        ps.setProblemCount(ps.getProblemCount() - 1);
        ps.setTotalScore(ps.getTotalScore() - item.getScore());
        baseMapper.updateById(ps);

        List<ProblemSetItem> remaining = problemSetItemMapper.selectList(
                new LambdaQueryWrapper<ProblemSetItem>()
                        .eq(ProblemSetItem::getSetId, setId)
                        .orderByAsc(ProblemSetItem::getSeqOrder)
        );
        int seq = 1;
        for (ProblemSetItem r : remaining) {
            if (r.getSeqOrder() != seq) {
                r.setSeqOrder(seq);
                problemSetItemMapper.updateById(r);
            }
            seq++;
        }
    }

/**
 * 重排题单题目顺序。
 */
    @Override
    @Transactional
    public void reorderItems(Long setId, List<Long> itemIds, Long userId) {
        ProblemSet ps = baseMapper.selectById(setId);
        if (ps == null) {
            throw new RuntimeException("题单不存在");
        }
        if (!ps.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此题单");
        }

        int seq = 1;
        for (Long itemId : itemIds) {
            ProblemSetItem item = problemSetItemMapper.selectById(itemId);
            if (item != null && item.getSetId().equals(setId)) {
                item.setSeqOrder(seq++);
                problemSetItemMapper.updateById(item);
            }
        }
    }

/**
 * 删除题单及其条目。
 */
    @Override
    @Transactional
    public void deleteProblemSet(Long setId, Long userId) {
        ProblemSet ps = baseMapper.selectById(setId);
        if (ps == null) {
            throw new RuntimeException("题单不存在");
        }
        if (!ps.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此题单");
        }

        baseMapper.deleteById(setId);
        log.info("删除题单：id={}", setId);
    }

/**
 * 更新题单基础信息。
 */
    @Override
    public void updateProblemSet(Long setId, CreateProblemSetDTO dto) {
        ProblemSet ps = baseMapper.selectById(setId);
        if (ps == null) {
            throw new RuntimeException("题单不存在");
        }
        if (!ps.getUserId().equals(dto.getUserId())) {
            throw new RuntimeException("无权操作此题单");
        }

        if (dto.getTitle() != null) {
            ps.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            ps.setDescription(dto.getDescription());
        }
        if (dto.getDifficultyLevel() != null) {
            ps.setDifficultyLevel(dto.getDifficultyLevel());
        }
        baseMapper.updateById(ps);
    }

    private Map<String, Integer> getDistribution(QuickGenerateDTO dto) {
        if ("custom".equals(dto.getDifficultyLevel()) && dto.getDistribution() != null) {
            return Map.of(
                    "Easy", dto.getDistribution().getEasy(),
                    "Medium", dto.getDistribution().getMedium(),
                    "Hard", dto.getDistribution().getHard()
            );
        }
        return switch (dto.getDifficultyLevel()) {
            case "beginner" -> BEGINNER_DIST;
            case "advanced" -> ADVANCED_DIST;
            default -> INTERMEDIATE_DIST;
        };
    }

    private List<Problem> randomPickProblems(String difficulty, int count, String ojPlatform,
                                             List<String> tags, Set<String> excludeSlugs) {
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<Problem>()
                .eq(Problem::getDifficulty, difficulty)
                .eq(Problem::getOjPlatform, ojPlatform);

        if (!excludeSlugs.isEmpty()) {
            wrapper.notIn(Problem::getSlug, excludeSlugs);
        }

        List<String> normalizedTags = normalizeTags(tags);
        if (!normalizedTags.isEmpty()) {
            List<Long> taggedProblemIds = problemTagRelationMapper
                    .selectProblemIdsByPlatformAndTags(ojPlatform, normalizedTags);
            if (taggedProblemIds.isEmpty()) {
                return List.of();
            }
            wrapper.in(Problem::getId, taggedProblemIds);
        }

        List<Problem> candidates = problemService.list(wrapper);
        Collections.shuffle(candidates);
        return candidates.stream().limit(count).collect(Collectors.toList());
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .filter(StringUtils::hasText)
                .map(tag -> tag.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }
}
