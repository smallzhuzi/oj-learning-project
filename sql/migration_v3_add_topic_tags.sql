USE oj_platform;

-- 为 problems 表添加 topic_tags 列（存储题目标签 JSON 数组）
ALTER TABLE problems
    ADD COLUMN topic_tags JSON DEFAULT NULL COMMENT '题目标签 JSON 数组，如 [{"name":"数组","slug":"array"}]'
    AFTER code_snippets;
