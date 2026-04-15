package com.ojplatform.dto;

import java.util.List;

/**
 * 力扣题目详情数据传输对象。
 */
public class LeetCodeProblemDetail {

    /**
     * 远程题目ID。
     */
    private String questionId;

    /**
     * 题目前端 ID。
     */
    private String questionFrontendId;

    /**
     * 标题。
     */
    private String title;

    /**
     * 翻译标题。
     */
    private String translatedTitle;

    /**
     * 翻译内容。
     */
    private String translatedContent;

    /**
     * 难度。
     */
    private String difficulty;

    /**
     * 统计。
     */
    private String stats;

    /**
     * 代码Snippets。
     */
    private List<CodeSnippet> codeSnippets;

    /**
     * 主题标签。
     */
    private List<TopicTag> topicTags;

    /**
     * 主题标签数据传输对象。
     */
    public static class TopicTag {
        /**
         * 唯一标识。
         */
        private String id;
        /**
         * 名称。
         */
        private String name;
        /**
         * 题目标识。
         */
        private String slug;
        /**
         * 类型。
         */
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

    /**
     * 代码Snippet数据传输对象。
     */
    public static class CodeSnippet {
        /**
         * 语言。
         */
        private String lang;
        /**
         * 语言标识。
         */
        private String langSlug;
        /**
         * 代码内容。
         */
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
