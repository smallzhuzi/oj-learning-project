package com.ojplatform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ojplatform.dto.OjProblemDetail;
import com.ojplatform.entity.Problem;
import com.ojplatform.mapper.ProblemMapper;
import com.ojplatform.service.OjApiService;
import com.ojplatform.service.OjApiServiceFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 洛谷全量题目爬虫
 *
 * 使用方式：
 *   cd backend
 *   mvn test -Dtest=LuoguCrawlerTest#crawlAll -pl .
 *
 * 可用测试方法：
 *   crawlAll              — 拉列表 + 多线程补详情
 *   crawlListOnly         — 仅拉列表基础信息
 *   fillDetails           — 单线程补全详情
 *   fillDetailsParallel   — 多线程补全详情（推荐，快 3~5 倍）
 *   fillTags              — 单线程补全中文标签
 *   fillTagsParallel      — 多线程补全中文标签（推荐）
 */
@SpringBootTest
public class LuoguCrawlerTest {

    private static final Logger log = LoggerFactory.getLogger(LuoguCrawlerTest.class);

    private static final String PLATFORM = "luogu";

    /** 洛谷每页固定 50 条 */
    private static final int PAGE_SIZE = 50;

    /** 请求间隔（毫秒） */
    private static final long LIST_SLEEP_MS = 800;
    private static final long DETAIL_SLEEP_MS = 1500;

    /** 并发线程数 */
    private static final int THREAD_COUNT = 4;

    @Autowired
    private OjApiServiceFactory ojApiServiceFactory;

    @Autowired
    private ProblemMapper problemMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 全量爬取：先拉列表，再多线程补详情
     */
    @Test
    public void crawlAll() throws Exception {
        log.info("========== 开始全量爬取洛谷题目 ==========");
        crawlListOnly();
        fillDetailsParallel();
        log.info("========== 全量爬取完成 ==========");
    }

    /**
     * 仅拉取列表（不含详情），快速入库所有题号
     */
    @Test
    public void crawlListOnly() throws Exception {
        log.info("========== 开始拉取洛谷题目列表 ==========");

        OjApiService api = ojApiServiceFactory.getService(PLATFORM);
        int skip = 0;
        int totalSaved = 0;
        int totalSkipped = 0;

        while (true) {
            log.info("--- 拉取列表：skip={}, limit={} ---", skip, PAGE_SIZE);
            List<Problem> page;
            try {
                page = api.fetchProblemList(skip, PAGE_SIZE, null);
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
                Problem existing = findBySlug(p.getSlug());
                if (existing != null) {
                    totalSkipped++;
                    continue;
                }
                p.setOjPlatform(PLATFORM);
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

        log.info("列表拉取完成：新增 {}，跳过 {}", totalSaved, totalSkipped);
    }

    /**
     * 多线程补全详情（推荐）
     * 将待补全题目分成 N 份，每个线程处理一份，各自保持请求间隔
     */
    @Test
    public void fillDetailsParallel() throws Exception {
        log.info("========== 开始多线程补全洛谷题目详情（{} 线程） ==========", THREAD_COUNT);

        List<Problem> incomplete = findIncomplete();
        if (incomplete.isEmpty()) {
            log.info("所有题目详情已完整，无需补全");
            return;
        }
        log.info("共 {} 道题目需要补全详情", incomplete.size());

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        AtomicInteger progress = new AtomicInteger(0);
        int total = incomplete.size();

        // 将题目列表按线程数分片
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<?>> futures = new java.util.ArrayList<>();

        int chunkSize = (total + THREAD_COUNT - 1) / THREAD_COUNT;
        for (int t = 0; t < THREAD_COUNT; t++) {
            int from = t * chunkSize;
            int to = Math.min(from + chunkSize, total);
            if (from >= total) break;

            List<Problem> chunk = incomplete.subList(from, to);
            int threadId = t + 1;

            futures.add(executor.submit(() -> {
                OjApiService api = ojApiServiceFactory.getService(PLATFORM);
                for (Problem p : chunk) {
                    int current = progress.incrementAndGet();
                    try {
                        OjProblemDetail detail = api.fetchProblemDetail(p.getSlug());
                        if (detail == null) {
                            log.warn("[线程{}] 详情返回 null：{}", threadId, p.getSlug());
                            failed.incrementAndGet();
                            continue;
                        }

                        updateProblemFromDetail(p, detail);
                        success.incrementAndGet();

                        if (current % 20 == 0) {
                            log.info("进度：{}/{} （成功 {}，失败 {}）", current, total, success.get(), failed.get());
                        }

                        Thread.sleep(DETAIL_SLEEP_MS);

                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        String msg = e.getMessage() != null ? e.getMessage() : "";
                        if (msg.contains("429") || msg.contains("频率")) {
                            log.warn("[线程{}] 被限流，等待 30 秒：{}", threadId, p.getSlug());
                            try { Thread.sleep(30000); } catch (InterruptedException ignored) { break; }
                            // 限流后不算失败，但也不重试（继续下一道）
                        } else {
                            log.warn("[线程{}] 拉取失败：{} — {}", threadId, p.getSlug(), msg);
                        }
                        failed.incrementAndGet();
                    }
                }
            }));
        }

        // 等待所有线程完成
        executor.shutdown();
        executor.awaitTermination(6, TimeUnit.HOURS);

        log.info("多线程详情补全完成：成功 {}，失败 {}，总计 {}", success.get(), failed.get(), total);
    }

    /**
     * 单线程补全详情（备用，更稳定但更慢）
     */
    @Test
    public void fillDetails() throws Exception {
        log.info("========== 开始单线程补全洛谷题目详情 ==========");

        OjApiService api = ojApiServiceFactory.getService(PLATFORM);
        List<Problem> incomplete = findIncomplete();
        if (incomplete.isEmpty()) {
            log.info("所有题目详情已完整，无需补全");
            return;
        }
        log.info("共 {} 道题目需要补全详情", incomplete.size());

        int success = 0;
        int failed = 0;

        for (int i = 0; i < incomplete.size(); i++) {
            Problem p = incomplete.get(i);
            log.info("[{}/{}] 拉取详情：{} — {}", i + 1, incomplete.size(), p.getSlug(), p.getTitle());

            try {
                OjProblemDetail detail = api.fetchProblemDetail(p.getSlug());
                if (detail == null) {
                    failed++;
                    continue;
                }
                updateProblemFromDetail(p, detail);
                success++;
                Thread.sleep(DETAIL_SLEEP_MS);

            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("429") || msg.contains("频率")) {
                    log.warn("被限流，等待 30 秒后重试...");
                    Thread.sleep(30000);
                    i--;
                } else {
                    log.warn("拉取详情失败：{} — {}", p.getSlug(), msg);
                    failed++;
                }
            }
        }

        log.info("详情补全完成：成功 {}，失败 {}", success, failed);
    }

    // ======================== 标签补全 ========================

    /**
     * 多线程补全洛谷题目中文标签（推荐）
     *
     * 从洛谷 API 的 data.tags 字段读取完整标签对象（含中文 name），
     * 写入数据库 topic_tags 列（JSON 数组格式）。
     *
     * 使用方式：
     *   mvn test -Dtest=LuoguCrawlerTest#fillTagsParallel -pl .
     */
    @Test
    public void fillTagsParallel() throws Exception {
        log.info("========== 开始多线程补全洛谷题目中文标签（{} 线程） ==========", THREAD_COUNT);

        List<Problem> noTags = findNoTags();
        if (noTags.isEmpty()) {
            log.info("所有洛谷题目标签已完整，无需补全");
            return;
        }
        log.info("共 {} 道题目需要补全标签", noTags.size());

        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);
        AtomicInteger progress = new AtomicInteger(0);
        int total = noTags.size();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        int chunkSize = (total + THREAD_COUNT - 1) / THREAD_COUNT;

        for (int t = 0; t < THREAD_COUNT; t++) {
            int from = t * chunkSize;
            int to = Math.min(from + chunkSize, total);
            if (from >= total) break;

            List<Problem> chunk = noTags.subList(from, to);
            int threadId = t + 1;

            executor.submit(() -> {
                OjApiService api = ojApiServiceFactory.getService(PLATFORM);
                for (Problem p : chunk) {
                    int current = progress.incrementAndGet();
                    try {
                        OjProblemDetail detail = api.fetchProblemDetail(p.getSlug());
                        if (detail != null && detail.getTopicTags() != null && !detail.getTopicTags().isEmpty()) {
                            String tagsJson = objectMapper.writeValueAsString(detail.getTopicTags());
                            p.setTopicTags(tagsJson);
                            problemMapper.updateById(p);
                            success.incrementAndGet();
                            log.debug("[线程{}] 标签写入成功：{} — {}", threadId, p.getSlug(), tagsJson);
                        } else {
                            log.debug("[线程{}] 题目无标签：{}", threadId, p.getSlug());
                        }

                        if (current % 50 == 0) {
                            log.info("标签进度：{}/{} （成功 {}，失败 {}）", current, total, success.get(), failed.get());
                        }
                        Thread.sleep(DETAIL_SLEEP_MS);

                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        String msg = e.getMessage() != null ? e.getMessage() : "";
                        if (msg.contains("429") || msg.contains("频率")) {
                            log.warn("[线程{}] 被限流，等待 30 秒：{}", threadId, p.getSlug());
                            try { Thread.sleep(30000); } catch (InterruptedException ignored) { break; }
                        } else {
                            log.warn("[线程{}] 标签拉取失败：{} — {}", threadId, p.getSlug(), msg);
                        }
                        failed.incrementAndGet();
                    }
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(6, TimeUnit.HOURS);

        log.info("标签补全完成：成功 {}，失败 {}，总计 {}", success.get(), failed.get(), total);
    }

    /**
     * 单线程补全洛谷题目中文标签（备用，更稳定但更慢）
     *
     * 使用方式：
     *   mvn test -Dtest=LuoguCrawlerTest#fillTags -pl .
     */
    @Test
    public void fillTags() throws Exception {
        log.info("========== 开始单线程补全洛谷题目中文标签 ==========");

        OjApiService api = ojApiServiceFactory.getService(PLATFORM);
        List<Problem> noTags = findNoTags();
        if (noTags.isEmpty()) {
            log.info("所有洛谷题目标签已完整，无需补全");
            return;
        }
        log.info("共 {} 道题目需要补全标签", noTags.size());

        int success = 0;
        int failed = 0;

        for (int i = 0; i < noTags.size(); i++) {
            Problem p = noTags.get(i);
            log.info("[{}/{}] 拉取标签：{} — {}", i + 1, noTags.size(), p.getSlug(), p.getTitle());

            try {
                OjProblemDetail detail = api.fetchProblemDetail(p.getSlug());
                if (detail != null && detail.getTopicTags() != null && !detail.getTopicTags().isEmpty()) {
                    String tagsJson = objectMapper.writeValueAsString(detail.getTopicTags());
                    p.setTopicTags(tagsJson);
                    problemMapper.updateById(p);
                    success++;
                    log.info("  标签：{}", tagsJson);
                } else {
                    log.info("  该题目无标签");
                }
                Thread.sleep(DETAIL_SLEEP_MS);

            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("429") || msg.contains("频率")) {
                    log.warn("被限流，等待 30 秒后重试...");
                    Thread.sleep(30000);
                    i--; // 重试当前题
                } else {
                    log.warn("拉取标签失败：{} — {}", p.getSlug(), msg);
                    failed++;
                }
            }
        }

        log.info("标签补全完成：成功 {}，失败 {}", success, failed);
    }

    // ======================== 辅助方法 ========================

    /** 查询所有缺少详情的洛谷题目 */
    private List<Problem> findIncomplete() {
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Problem::getOjPlatform, PLATFORM)
               .isNull(Problem::getContentMarkdown);
        return problemMapper.selectList(wrapper);
    }

    /** 查询所有缺少标签的洛谷题目 */
    private List<Problem> findNoTags() {
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Problem::getOjPlatform, PLATFORM)
               .isNull(Problem::getTopicTags);
        return problemMapper.selectList(wrapper);
    }

    /** 用详情数据更新题目记录 */
    private synchronized void updateProblemFromDetail(Problem p, OjProblemDetail detail) {
        try {
            if (detail.getContent() != null) {
                p.setContentMarkdown(detail.getContent());
            }
            if (detail.getTitle() != null && !detail.getTitle().isBlank()) {
                p.setTitle(detail.getTitle());
            }
            if (detail.getAcceptanceRate() != null && p.getAcceptanceRate() == null) {
                p.setAcceptanceRate(detail.getAcceptanceRate());
            }
            if (detail.getTopicTags() != null && !detail.getTopicTags().isEmpty()) {
                p.setTopicTags(objectMapper.writeValueAsString(detail.getTopicTags()));
            }
            problemMapper.updateById(p);
        } catch (Exception e) {
            log.warn("更新题目数据库失败：{} — {}", p.getSlug(), e.getMessage());
        }
    }

    private Problem findBySlug(String slug) {
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Problem::getSlug, slug)
               .eq(Problem::getOjPlatform, PLATFORM);
        return problemMapper.selectOne(wrapper);
    }
}
