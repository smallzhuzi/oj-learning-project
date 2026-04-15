package com.ojplatform.service;

import com.ojplatform.dto.DifyChatResponse;
import com.ojplatform.dto.SessionTrackItemDTO;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Dify 接口相关业务接口。
 */
public interface DifyApiService {

    /**
     * 推荐下一题（流式）
     */
    DifyChatResponse recommendNext(Long sessionId, String currentSlug, List<SessionTrackItemDTO> history,
                                    String currentDifficulty, String currentTopicTags, String submissionSummary,
                                    String ojPlatform, Consumer<String> onChunk);

    /**
     * 分析代码提交（流式）
     */
    DifyChatResponse analyzeSubmission(Long sessionId, String problemSlug,
                                        String code, String language,
                                        String status, String runtime, String memory,
                                        String topicTags, Integer totalCorrect, Integer totalTestcases,
                                        Consumer<String> onChunk);

    /**
     * 渐进式提示（流式）
     */
    DifyChatResponse requestHint(Long sessionId, String problemSlug,
                                  Integer hintLevel, String topicTags, String difficulty,
                                  Consumer<String> onChunk);

    /**
     * 主动提问（流式）
     */
    DifyChatResponse askTeacher(Long sessionId, String question, Consumer<String> onChunk);

    /**
     * 获取会话的 Dify 历史消息
     * @return 消息列表（每条含 query 和 answer）
     */
    List<Map<String, Object>> getConversationMessages(Long sessionId);

    /**
     * 智能组题（流式）
     * 调用独立的智能组题 Dify 应用，根据用户水平和目标推荐题目组合
     * @param userId 用户 ID
     * @param query 组合后的用户需求描述
     * @param inputs 输入变量（count, self_assessment, target_goal 等）
     * @param onChunk 流式回调
     * @return Dify 响应（answer 中含 [PROBLEM_SET]...[/PROBLEM_SET] 结构化数据）
     */
    DifyChatResponse smartGenerate(Long userId, String query, Map<String, String> inputs, Consumer<String> onChunk);

    /**
     * 赛后分析（流式）
     * 调用独立的赛后分析 Dify 应用
     */
    DifyChatResponse contestAnalysis(Long userId, String query, Map<String, String> inputs, Consumer<String> onChunk);
}
