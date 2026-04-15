package com.ojplatform.service;

import com.ojplatform.dto.OjJudgeResult;
import com.ojplatform.dto.OjProblemDetail;
import com.ojplatform.entity.Problem;

import java.util.List;

/**
 * 判题接口相关业务接口。
 */
public interface OjApiService {

    /**
     * 获取该实现支持的平台标识
     * @return 平台标识（如 "leetcode"、"luogu"）
     */
    String getPlatform();

    /**
     * 拉取题目列表
     * @param skip    跳过的题目数（分页偏移量）
     * @param limit   每页数量
     * @param keyword 搜索关键词（可选）
     * @return 题目列表（基础信息）
     */
    List<Problem> fetchProblemList(int skip, int limit, String keyword);

    /**
     * 拉取单个题目的完整详情
     * @param slug 题目标识（LeetCode: two-sum / 洛谷: P1001）
     * @return 统一的题目详情 DTO
     */
    OjProblemDetail fetchProblemDetail(String slug);

    /**
     * 提交代码到远程 OJ
     * @param slug       题目标识
     * @param questionId 平台内部题目 ID
     * @param lang       编程语言（已经过 mapLanguage 转换的平台值）
     * @param code       用户代码
     * @return 远程提交 ID
     */
    String submitCode(String slug, String questionId, String lang, String code);

    /**
     * 轮询判题结果
     * @param remoteSubmissionId 远程提交 ID
     * @return 统一的判题结果 DTO
     */
    OjJudgeResult checkResult(String remoteSubmissionId);

    /**
     * 将通用语言 slug 映射为平台 API 所需的语言标识
     * @param commonLangSlug 通用语言 slug（java / python3 / cpp）
     * @return 平台所需的语言标识（LeetCode: java / 洛谷: 28）
     */
    String mapLanguage(String commonLangSlug);
}
