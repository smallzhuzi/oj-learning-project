package com.ojplatform.dto;

/**
 * Dify 聊天响应 DTO
 * 封装 Dify API 返回的对话结果
 */
public class DifyChatResponse {

    /** Dify 消息 ID */
    private String messageId;

    /** Dify 对话 ID（首次对话由 Dify 生成，后续复用） */
    private String conversationId;

    /** AI 回复文本 */
    private String answer;

    /** 推荐的下一题 slug（仅 recommend_next 类型有值） */
    private String nextProblemSlug;

    /** 创建时间戳 */
    private Long createdAt;

    // ==================== Getter / Setter ====================

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getNextProblemSlug() {
        return nextProblemSlug;
    }

    public void setNextProblemSlug(String nextProblemSlug) {
        this.nextProblemSlug = nextProblemSlug;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
