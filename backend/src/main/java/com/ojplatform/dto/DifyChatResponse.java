package com.ojplatform.dto;

/**
 * Dify 对话响应数据传输对象。
 */
public class DifyChatResponse {

    /**
     * 消息 ID。
     */
    private String messageId;

    /**
     * 会话ID。
     */
    private String conversationId;

    /**
     * 回答内容。
     */
    private String answer;

    /**
     * 下一题目标识。
     */
    private String nextProblemSlug;

    /**
     * 创建时间。
     */
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
