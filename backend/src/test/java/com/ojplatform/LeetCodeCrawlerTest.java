package com.ojplatform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojplatform.dto.LeetCodeProblemDetail;
import com.ojplatform.entity.Problem;
import com.ojplatform.mapper.ProblemMapper;
import com.ojplatform.service.LeetCodeApiService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

/**
 * LeetCode 全量题目爬虫
 *
 * 使用方式：
 *   cd backend
 *   mvn test -Dtest=LeetCodeCrawlerTest#crawlAll -pl .
 *
 * 流程：
 *   1. 先用 fetchProblemList 分页拉取所有题目基础信息（slug、题号、难度、通过率、标签）
 *   2. 对每道题再调 fetchProblemDetail 拉取完整信息（描述、代码模板、questionId）
 *   3. 全部写入本地数据库，已存在的跳过或补全缺失字段
 *   4. 每次请求之间休眠，防止被 LeetCode 限流
 */
@SpringBootTest
public class LeetCodeCrawlerTest {

    private static final Logger log = LoggerFactory.getLogger(LeetCodeCrawlerTest.class);

    /** 列表接口每页拉取数量 */
    private static final int PAGE_SIZE = 50;

    /** 每次 HTTP 请求之间的休眠毫秒数（防止 429 限流） */
    private static final long LIST_SLEEP_MS = 500;
    private static final long DETAIL_SLEEP_MS = 1200;

    @Autowired
    private LeetCodeApiService leetCodeApiService;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 全量爬取：先拉列表，再逐题拉详情，全部入库
     */
    @Test
    public void crawlAll() throws Exception {
        log.info("========== 开始全量爬取 LeetCode 题目 ==========");

        // ---- 第一阶段：分页拉取题目列表 ----
        int skip = 0;
        int totalSaved = 0;
        int totalSkipped = 0;

        while (true) {
            log.info("--- 拉取列表：skip={}, limit={} ---", skip, PAGE_SIZE);
            List<Problem> page;
            try {
                page = leetCodeApiService.fetchProblemList(skip, PAGE_SIZE, null);
            } catch (Exception e) {
                log.error("拉取列表失败（skip={}），等待 10 秒后重试...", skip, e);
                Thread.sleep(10000);
                continue;
            }

            if (page.isEmpty()) {
                log.info("已无更多题目，列表拉取完毕");
                break;
            }

            for (Problem p : page) {
                // 检查本地是否已存在
                Problem existing = findBySlug(p.getSlug());
                if (existing != null) {
                    totalSkipped++;
                    continue;
                }

                // 插入基础信息（slug、题号、难度、通过率）
                p.setOjPlatform("leetcode");
                problemMapper.insert(p);
                totalSaved++;
            }

            log.info("本批次 {} 条，累计新增 {}，跳过 {}", page.size(), totalSaved, totalSkipped);

            if (page.size() < PAGE_SIZE) {
                log.info("本页不足 {} 条，列表拉取完毕", PAGE_SIZE);
                break;
            }

            skip += PAGE_SIZE;
            Thread.sleep(LIST_SLEEP_MS);
        }

        log.info("第一阶段完成：共新增 {} 道，跳过 {} 道", totalSaved, totalSkipped);

        // ---- 第二阶段：逐题补全详情 ----
        fillDetails();

        log.info("========== 全量爬取完成 ==========");
    }

    /**
     * 仅补全详情（适合列表已拉完、详情中途断了的情况）
     * 找出所有 content_markdown 或 question_id 为空的题目，逐一拉取详情
     */
    @Test
    public void fillDetails() throws Exception {
        log.info("========== 开始补全题目详情 ==========");

        // 查出所有缺少详情的题目
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Problem::getOjPlatform, "leetcode")
               .and(w -> w
                   .isNull(Problem::getQuestionId)
                   .or()
                   .isNull(Problem::getContentMarkdown)
                   .or()
                   .isNull(Problem::getCodeSnippets)
                   .or()
                   .isNull(Problem::getTopicTags)
               );
        List<Problem> incomplete = problemMapper.selectList(wrapper);
        log.info("共 {} 道题目需要补全详情", incomplete.size());

        int success = 0;
        int failed = 0;
        int retryCount = 0;
        int maxRetries = 3;

        for (int i = 0; i < incomplete.size(); i++) {
            Problem p = incomplete.get(i);
            log.info("[{}/{}] 拉取详情：{} ({})", i + 1, incomplete.size(), p.getSlug(), p.getFrontendId());

            try {
                LeetCodeProblemDetail detail = leetCodeApiService.fetchProblemDetail(p.getSlug());
                if (detail == null) {
                    log.warn("详情返回 null，跳过：{}", p.getSlug());
                    failed++;
                    continue;
                }

                // 补全所有字段
                if (p.getQuestionId() == null) {
                    p.setQuestionId(detail.getQuestionId());
                }
                if (p.getContentMarkdown() == null) {
                    p.setContentMarkdown(detail.getTranslatedContent());
                }
                if (p.getTitle() == null || p.getTitle().equals(detail.getTitle())) {
                    // 用中文标题覆盖英文标题
                    String zhTitle = detail.getTranslatedTitle();
                    if (zhTitle != null && !zhTitle.isBlank()) {
                        p.setTitle(zhTitle);
                    }
                }
                if (p.getCodeSnippets() == null && detail.getCodeSnippets() != null && !detail.getCodeSnippets().isEmpty()) {
                    p.setCodeSnippets(objectMapper.writeValueAsString(detail.getCodeSnippets()));
                }
                if (p.getTopicTags() == null && detail.getTopicTags() != null && !detail.getTopicTags().isEmpty()) {
                    p.setTopicTags(objectMapper.writeValueAsString(detail.getTopicTags()));
                }
                if (p.getAcceptanceRate() == null && detail.getStats() != null) {
                    p.setAcceptanceRate(parseAcceptanceRate(detail.getStats()));
                }
                if (p.getFrontendId() == null) {
                    p.setFrontendId(detail.getQuestionFrontendId());
                }

                problemMapper.updateById(p);
                success++;
                retryCount = 0; // 重置重试计数

                Thread.sleep(DETAIL_SLEEP_MS);

            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("429") || msg.contains("频率")) {
                    // 被限流，等待更长时间后重试
                    retryCount++;
                    if (retryCount > maxRetries) {
                        log.error("连续 {} 次被限流，终止爬取。已成功 {}，失败 {}", maxRetries, success, failed);
                        break;
                    }
                    long waitMs = 30000L * retryCount;
                    log.warn("被限流，等待 {} 秒后重试（第 {} 次）...", waitMs / 1000, retryCount);
                    Thread.sleep(waitMs);
                    i--; // 重试当前题目
                } else if (msg.contains("403") || msg.contains("认证失败")) {
                    log.error("认证失败！请检查 LEETCODE_SESSION 和 CSRF Token 是否过期。已成功 {}，失败 {}", success, failed);
                    break;
                } else {
                    log.warn("拉取详情失败：{} — {}", p.getSlug(), msg);
                    failed++;
                    Thread.sleep(DETAIL_SLEEP_MS);
                }
            }
        }

        log.info("详情补全完成：成功 {}，失败 {}", success, failed);
    }

    /**
     * 仅拉取列表（不含详情），适合先快速入库所有题号
     */
    @Test
    public void crawlListOnly() throws Exception {
        log.info("========== 开始拉取题目列表（仅基础信息） ==========");

        int skip = 0;
        int totalSaved = 0;
        int totalSkipped = 0;

        while (true) {
            log.info("--- skip={}, limit={} ---", skip, PAGE_SIZE);
            List<Problem> page;
            try {
                page = leetCodeApiService.fetchProblemList(skip, PAGE_SIZE, null);
            } catch (Exception e) {
                log.error("拉取失败（skip={}），等待 10 秒后重试...", skip, e);
                Thread.sleep(10000);
                continue;
            }

            if (page.isEmpty()) break;

            for (Problem p : page) {
                Problem existing = findBySlug(p.getSlug());
                if (existing != null) {
                    totalSkipped++;
                    continue;
                }
                p.setOjPlatform("leetcode");
                problemMapper.insert(p);
                totalSaved++;
            }

            log.info("累计新增 {}，跳过 {}", totalSaved, totalSkipped);

            if (page.size() < PAGE_SIZE) break;
            skip += PAGE_SIZE;
            Thread.sleep(LIST_SLEEP_MS);
        }

        log.info("列表拉取完成：新增 {}，跳过 {}", totalSaved, totalSkipped);
    }

    // ======================== 辅助方法 ========================

    private Problem findBySlug(String slug) {
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Problem::getSlug, slug)
               .eq(Problem::getOjPlatform, "leetcode");
        return problemMapper.selectOne(wrapper);
    }

    private BigDecimal parseAcceptanceRate(String statsJson) {
        try {
            var stats = objectMapper.readTree(statsJson);
            String acRate = stats.path("acRate").asText("0");
            return new BigDecimal(acRate.replace("%", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
