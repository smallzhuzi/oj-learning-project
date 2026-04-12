package com.ojplatform.service;

import com.ojplatform.dto.LeetCodeJudgeResult;
import com.ojplatform.dto.LeetCodeProblemDetail;
import com.ojplatform.entity.Problem;

import java.util.List;

/**
 * LeetCode API 调用服务接口
 * 封装所有与 LeetCode CN 的 HTTP 交互
 */
public interface LeetCodeApiService {

    /**
     * 从 LeetCode 拉取题目列表（GraphQL）
     *
     * @param skip    跳过的题目数（分页偏移量）
     * @param limit   每页数量
     * @param keyword 搜索关键词（可选，为空则查全部）
     * @return 题目列表（仅基础信息，不含题目描述和 questionId）
     */
    List<Problem> fetchProblemList(int skip, int limit, String keyword);

    /**
     * 从 LeetCode 拉取单个题目的完整详情（GraphQL）
     * 包含 questionId、题目描述、代码模板等
     *
     * @param titleSlug 题目 slug（如 two-sum）
     * @return 题目详情 DTO
     */
    LeetCodeProblemDetail fetchProblemDetail(String titleSlug);

    /**
     * 提交代码到 LeetCode
     *
     * @param slug       题目 slug（构造提交 URL）
     * @param questionId LeetCode 内部题目 ID
     * @param lang       编程语言 slug（java / python3 / cpp）
     * @param typedCode  用户代码
     * @return LeetCode 返回的远程提交 ID
     */
    String submitCode(String slug, String questionId, String lang, String typedCode);

    /**
     * 轮询 LeetCode 判题结果
     *
     * @param submissionId LeetCode 远程提交 ID
     * @return 判题结果（若仍在判题中，finished 为 false）
     */
    LeetCodeJudgeResult checkResult(String submissionId);
}
