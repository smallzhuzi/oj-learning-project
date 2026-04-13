package com.ojplatform.dto;

import java.util.List;

/**
 * LeetCode 题目详情 DTO
 */
public class LeetCodeProblemDetail {

    /** LeetCode 内部题目 ID */
    private String questionId;

    /** 前端展示题号 */
    private String questionFrontendId;

    /** 英文标题 */
    private String title;

    /** 中文标题 */
    private String translatedTitle;

    /** 中文题面 HTML */
    private String translatedContent;

    /** 难度 */
    private String difficulty;

    /** 统计信息 JSON */
    private String stats;

    /** 各语言初始代码 */
    private List<CodeSnippet> codeSnippets;

    /** 题目标签列表 */
    private List<TopicTag> topicTags;

    public static class TopicTag {
        /** 平台原始标签 ID */
        private String id;
        /** 标签显示名 */
        private String name;
        /** 标签 slug */
        private String slug;
        /** 平台原始标签类型，例如洛谷的 1..6 */
        private Integer type;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

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

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }
    }

    public static class CodeSnippet {
        /** 语言显示名 */
        private String lang;
        /** 语言 slug */
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
