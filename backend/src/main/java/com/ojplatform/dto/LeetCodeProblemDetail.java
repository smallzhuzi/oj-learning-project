package com.ojplatform.dto;

import java.util.List;

/**
 * LeetCode 题目详情 DTO
 * 对应 GraphQL questionData 查询的返回结果
 */
public class LeetCodeProblemDetail {

    /** LeetCode 内部题目 ID（提交代码时必需） */
    private String questionId;

    /** 前端展示的题号（如 "1"） */
    private String questionFrontendId;

    /** 题目英文标题 */
    private String title;

    /** 题目中文标题 */
    private String translatedTitle;

    /** 题目中文描述（HTML 格式） */
    private String translatedContent;

    /** 难度等级（Easy / Medium / Hard） */
    private String difficulty;

    /** 统计信息 JSON 字符串（包含 acRate 等） */
    private String stats;

    /** 各语言的初始代码模板 */
    private List<CodeSnippet> codeSnippets;

    /** 题目标签列表 */
    private List<TopicTag> topicTags;

    /**
     * 题目标签内部类
     */
    public static class TopicTag {
        /** 标签名称（如 "数组"） */
        private String name;
        /** 标签 slug（如 "array"） */
        private String slug;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }
    }

    /**
     * 代码模板内部类
     */
    public static class CodeSnippet {
        /** 语言显示名（如 "Java"） */
        private String lang;
        /** 语言 slug（如 "java"），提交时使用 */
        private String langSlug;
        /** 初始代码模板 */
        private String code;

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public String getLangSlug() {
            return langSlug;
        }

        public void setLangSlug(String langSlug) {
            this.langSlug = langSlug;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    // ==================== Getter / Setter ====================

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestionFrontendId() {
        return questionFrontendId;
    }

    public void setQuestionFrontendId(String questionFrontendId) {
        this.questionFrontendId = questionFrontendId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTranslatedTitle() {
        return translatedTitle;
    }

    public void setTranslatedTitle(String translatedTitle) {
        this.translatedTitle = translatedTitle;
    }

    public String getTranslatedContent() {
        return translatedContent;
    }

    public void setTranslatedContent(String translatedContent) {
        this.translatedContent = translatedContent;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getStats() {
        return stats;
    }

    public void setStats(String stats) {
        this.stats = stats;
    }

    public List<CodeSnippet> getCodeSnippets() {
        return codeSnippets;
    }

    public void setCodeSnippets(List<CodeSnippet> codeSnippets) {
        this.codeSnippets = codeSnippets;
    }

    public List<TopicTag> getTopicTags() {
        return topicTags;
    }

    public void setTopicTags(List<TopicTag> topicTags) {
        this.topicTags = topicTags;
    }
}
