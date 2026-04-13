-- ============================================================
-- v4 标签体系重构
-- 目标：
-- 1. 保留 problems.topic_tags 作为原始标签快照
-- 2. 新增统一标签类型、标签字典、平台标签映射、题目标签关联
-- 3. 为后续跨平台展示、筛选、画像分析、智能推荐提供统一标签体系
-- ============================================================

USE oj_platform;

-- 1. 标签类型表：算法 / 数据结构 / 来源 / 场景 / 平台元信息
CREATE TABLE IF NOT EXISTS tag_types (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '标签类型主键',
    type_key     VARCHAR(50)  NOT NULL                COMMENT '类型键，如 algorithm / data_structure / source',
    type_name    VARCHAR(100) NOT NULL                COMMENT '类型显示名，如 算法 / 数据结构 / 来源',
    description  VARCHAR(255) DEFAULT NULL            COMMENT '类型说明',
    sort_order   INT          NOT NULL DEFAULT 0      COMMENT '排序值',
    status       VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT '状态：active / disabled',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_types_key (type_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签类型表';

-- 2. 统一标签字典：平台无关的系统标签
CREATE TABLE IF NOT EXISTS tags (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '统一标签主键',
    tag_type_id     BIGINT       NOT NULL                COMMENT '标签类型 ID',
    tag_key         VARCHAR(100) NOT NULL                COMMENT '统一标签键，如 dp / array / luogu-noip',
    display_name    VARCHAR(100) NOT NULL                COMMENT '显示名称，如 动态规划 / 数组 / 洛谷 NOIP',
    alias_names     JSON         DEFAULT NULL            COMMENT '别名列表 JSON',
    description     VARCHAR(255) DEFAULT NULL            COMMENT '标签说明',
    color           VARCHAR(30)  DEFAULT NULL            COMMENT '展示色值，如 #3b82f6',
    icon            VARCHAR(50)  DEFAULT NULL            COMMENT '预留图标字段',
    parent_id       BIGINT       DEFAULT NULL            COMMENT '父标签 ID，支持层级关系',
    sort_order      INT          NOT NULL DEFAULT 0      COMMENT '排序值',
    status          VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT '状态：active / disabled',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_tags_type_key (tag_type_id, tag_key),
    INDEX idx_tags_parent_id (parent_id),
    CONSTRAINT fk_tags_type FOREIGN KEY (tag_type_id) REFERENCES tag_types(id),
    CONSTRAINT fk_tags_parent FOREIGN KEY (parent_id) REFERENCES tags(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统一标签字典表';

-- 3. 平台标签映射：保存不同 OJ 平台的原始标签，并映射到统一标签
CREATE TABLE IF NOT EXISTS platform_tags (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '平台标签主键',
    oj_platform      VARCHAR(30)  NOT NULL                COMMENT '平台标识，如 leetcode / luogu',
    source_tag_id    VARCHAR(100) DEFAULT NULL            COMMENT '平台原始标签 ID，如 洛谷数字 ID',
    source_slug      VARCHAR(150) DEFAULT NULL            COMMENT '平台原始 slug，如 dynamic-programming',
    source_name      VARCHAR(150) NOT NULL                COMMENT '平台原始标签名，如 动态规划',
    normalized_key   VARCHAR(100) DEFAULT NULL            COMMENT '归一化键，便于去重匹配',
    tag_type_id      BIGINT       DEFAULT NULL            COMMENT '平台标签推断类型',
    tag_id           BIGINT       DEFAULT NULL            COMMENT '映射到统一标签 ID，可为空表示待人工映射',
    metadata         JSON         DEFAULT NULL            COMMENT '扩展元数据',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_platform_tag_source (oj_platform, source_tag_id, source_slug, source_name),
    INDEX idx_platform_tags_key (normalized_key),
    INDEX idx_platform_tags_tag_id (tag_id),
    CONSTRAINT fk_platform_tags_type FOREIGN KEY (tag_type_id) REFERENCES tag_types(id),
    CONSTRAINT fk_platform_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台标签映射表';

-- 4. 题目标签关联：题目和统一标签 / 平台标签的多对多关系
CREATE TABLE IF NOT EXISTS problem_tag_relations (
    id               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关系主键',
    problem_id       BIGINT   NOT NULL                COMMENT '题目 ID',
    tag_id           BIGINT   DEFAULT NULL            COMMENT '统一标签 ID',
    platform_tag_id  BIGINT   DEFAULT NULL            COMMENT '平台标签 ID',
    created_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_problem_tag_relation (problem_id, tag_id, platform_tag_id),
    INDEX idx_ptr_problem_id (problem_id),
    INDEX idx_ptr_tag_id (tag_id),
    INDEX idx_ptr_platform_tag_id (platform_tag_id),
    CONSTRAINT fk_ptr_problem FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE CASCADE,
    CONSTRAINT fk_ptr_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    CONSTRAINT fk_ptr_platform_tag FOREIGN KEY (platform_tag_id) REFERENCES platform_tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目标签关联表';

-- 5. 初始化标签类型
INSERT INTO tag_types (type_key, type_name, description, sort_order)
VALUES
    ('algorithm', '算法', '算法思想类标签，如动态规划、贪心、二分', 10),
    ('data_structure', '数据结构', '数据结构类标签，如数组、哈希表、堆', 20),
    ('source', '来源', '题目来源或竞赛来源标签', 30),
    ('scenario', '场景', '题型或场景标签，如模拟、交互、数学建模', 40),
    ('platform_meta', '平台元信息', '仅平台内部使用的元信息标签', 50)
ON DUPLICATE KEY UPDATE
    type_name = VALUES(type_name),
    description = VALUES(description),
    sort_order = VALUES(sort_order);

-- 6. 迁移说明
-- 现阶段保留 problems.topic_tags 原始 JSON，不做破坏性删除。
-- 后续建议通过应用层脚本完成以下过程：
--   a. 扫描 problems.topic_tags
--   b. 写入 platform_tags
--   c. 建立 platform_tags -> tags 映射
--   d. 回填 problem_tag_relations
--
-- 推荐的统一标签策略：
--   - LeetCode 原始 slug 优先作为 normalized_key
--   - 洛谷数字标签 ID 只作为 source_tag_id 保存，不直接用于前端展示
--   - 前端统一展示 tags.display_name，不直接展示 source_slug/source_tag_id
