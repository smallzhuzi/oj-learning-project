package com.ojplatform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojplatform.dto.OjProblemDetail;
import com.ojplatform.dto.ProblemQueryDTO;
import com.ojplatform.entity.Problem;
import com.ojplatform.mapper.ProblemMapper;
import com.ojplatform.service.OjApiService;
import com.ojplatform.service.OjApiServiceFactory;
import com.ojplatform.service.ProblemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 题目服务实现类
 */
@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemService {

    private static final Logger log = LoggerFactory.getLogger(ProblemServiceImpl.class);

    @Autowired
    private OjApiServiceFactory ojApiServiceFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public IPage<Problem> queryProblems(ProblemQueryDTO queryDTO) {
        Page<Problem> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();

        // 列表查询排除题目描述（减少数据传输量）
        wrapper.select(
                Problem::getId, Problem::getSlug, Problem::getTitle,
                Problem::getDifficulty, Problem::getAcceptanceRate,
                Problem::getOjPlatform, Problem::getFrontendId,
                Problem::getQuestionId, Problem::getCreatedAt, Problem::getUpdatedAt
        );

        // 按 OJ 平台筛选
        wrapper.eq(Problem::getOjPlatform, queryDTO.getOjPlatform());

        // 按关键词模糊搜索（匹配题号或标题）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword().trim();
            wrapper.and(w -> w
                    .like(Problem::getTitle, keyword)
                    .or()
                    .like(Problem::getFrontendId, keyword)
            );
        }

        // 按难度筛选
        if (StringUtils.hasText(queryDTO.getDifficulty())) {
            wrapper.eq(Problem::getDifficulty, queryDTO.getDifficulty());
        }

        // 按题号数值升序
        wrapper.last("ORDER BY id ASC");

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Problem getBySlug(String slug, String ojPlatform) {
        // 1. 先查本地缓存
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Problem::getSlug, slug)
               .eq(Problem::getOjPlatform, ojPlatform);
        Problem problem = baseMapper.selectOne(wrapper);

        // 2. 本地未命中且平台支持远程拉取
        if (problem == null && ojApiServiceFactory.supports(ojPlatform)) {
            try {
                OjApiService apiService = ojApiServiceFactory.getService(ojPlatform);
                OjProblemDetail detail = apiService.fetchProblemDetail(slug);
                if (detail != null) {
                    problem = convertDetailToProblem(detail, slug, ojPlatform);
                    baseMapper.insert(problem);
                    log.info("从 {} 远程拉取并缓存题目：{} - {}", ojPlatform, slug, detail.getTitle());
                }
            } catch (Exception e) {
                log.warn("从 {} 远程拉取题目失败：slug={}, 原因={}", ojPlatform, slug, e.getMessage());
            }
        }

        // 3. 已缓存但缺少题目描述的题目，从远程补拉（仅补拉 contentMarkdown，codeSnippets 和 topicTags 为可选字段）
        if (problem != null && ojApiServiceFactory.supports(ojPlatform)
                && problem.getContentMarkdown() == null) {
            try {
                OjApiService apiService = ojApiServiceFactory.getService(ojPlatform);
                OjProblemDetail detail = apiService.fetchProblemDetail(slug);
                if (detail != null) {
                    boolean updated = false;
                    if (problem.getContentMarkdown() == null && detail.getContent() != null) {
                        problem.setContentMarkdown(detail.getContent());
                        updated = true;
                    }
                    if (problem.getCodeSnippets() == null
                            && detail.getCodeSnippets() != null && !detail.getCodeSnippets().isEmpty()) {
                        problem.setCodeSnippets(objectMapper.writeValueAsString(detail.getCodeSnippets()));
                        updated = true;
                    }
                    if (problem.getTopicTags() == null
                            && detail.getTopicTags() != null && !detail.getTopicTags().isEmpty()) {
                        problem.setTopicTags(objectMapper.writeValueAsString(detail.getTopicTags()));
                        updated = true;
                    }
                    if (updated) {
                        baseMapper.updateById(problem);
                        log.info("补拉题目数据成功：{}", slug);
                    }
                }
            } catch (Exception e) {
                log.warn("补拉题目数据失败：slug={}, 原因={}", slug, e.getMessage());
            }
        }

        return problem;
    }

    /**
     * 将统一的 OjProblemDetail 转换为 Problem 实体
     */
    private Problem convertDetailToProblem(OjProblemDetail detail, String slug, String ojPlatform) {
        Problem problem = new Problem();
        problem.setSlug(slug);
        problem.setTitle(detail.getTitle());
        problem.setDifficulty(detail.getDifficulty());
        problem.setOjPlatform(ojPlatform);
        problem.setContentMarkdown(detail.getContent());
        problem.setFrontendId(detail.getFrontendId());
        problem.setQuestionId(detail.getQuestionId());
        problem.setAcceptanceRate(detail.getAcceptanceRate());
        try {
            if (detail.getCodeSnippets() != null && !detail.getCodeSnippets().isEmpty()) {
                problem.setCodeSnippets(objectMapper.writeValueAsString(detail.getCodeSnippets()));
            }
            if (detail.getTopicTags() != null && !detail.getTopicTags().isEmpty()) {
                problem.setTopicTags(objectMapper.writeValueAsString(detail.getTopicTags()));
            }
        } catch (Exception e) {
            log.warn("序列化题目附加数据失败：{}", e.getMessage());
        }
        return problem;
    }
}
