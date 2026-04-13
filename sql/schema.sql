-- ============================================================
-- AI 智能 OJ 学习平台 - 数据库初始化脚本
-- 数据库：MySQL 8
-- 字符集：utf8mb4（支持 emoji 和特殊字符）
-- ============================================================

CREATE DATABASE IF NOT EXISTS oj_platform
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE oj_platform;

-- ============================================================
-- 1. 用户表 (users)
-- 说明：存储平台注册用户的基本信息
-- ============================================================
CREATE TABLE users (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户唯一标识',
    username    VARCHAR(50)  NOT NULL                COMMENT '用户名（登录名，不可重复）',
    email       VARCHAR(100) NOT NULL                COMMENT '用户邮箱（不可重复，用于找回密码等）',
    password    VARCHAR(255) NOT NULL                COMMENT '加密后的密码（BCrypt）',
    role        VARCHAR(20)  NOT NULL DEFAULT 'user' COMMENT '用户角色（user/admin）',
    status      VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT '账号状态（active/disabled）',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 题目表 (problems)
-- 说明：缓存从远程 OJ（如 LeetCode）拉取的题目信息，
--       避免每次都请求远程接口，提高响应速度
-- ============================================================
CREATE TABLE problems (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '题目本地唯一标识',
    slug             VARCHAR(200) NOT NULL                COMMENT '题目 slug（如 two-sum），用于构造 URL 和 API 调用',
    title            VARCHAR(200) NOT NULL                COMMENT '题目标题（如"两数之和"）',
    difficulty       VARCHAR(30)  NOT NULL                COMMENT '难度等级：LeetCode(Easy/Medium/Hard) 或 洛谷(入门/普及-/...)',
    acceptance_rate  DECIMAL(5,2) DEFAULT NULL            COMMENT '通过率（百分比，如 49.50 表示 49.50%）',
    oj_platform      VARCHAR(30)  NOT NULL DEFAULT 'leetcode' COMMENT 'OJ 平台标识（leetcode / codeforces 等，当前仅 leetcode）',
    content_markdown TEXT         DEFAULT NULL            COMMENT '题目描述（Markdown/HTML），从远程拉取后缓存',
    code_snippets    JSON         DEFAULT NULL            COMMENT '各语言初始代码模板 JSON 数组',
    frontend_id      VARCHAR(20)  DEFAULT NULL            COMMENT 'LeetCode 前端展示的题号（如 "1"、"2"）',
    question_id      VARCHAR(20)  DEFAULT NULL            COMMENT 'LeetCode 内部题目 ID（提交代码时需要）',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次入库时间',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_slug_platform (slug, oj_platform),
    INDEX idx_difficulty (difficulty),
    INDEX idx_frontend_id (frontend_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目缓存表';

-- ============================================================
-- 2.1 标签类型表 (tag_types)
-- 说明：定义系统级标签分类，如算法、数据结构、来源、场景等
-- ============================================================
CREATE TABLE tag_types (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '标签类型主键',
    type_key     VARCHAR(50)  NOT NULL                COMMENT '类型键，如 algorithm / data_structure / source',
    type_name    VARCHAR(100) NOT NULL                COMMENT '类型显示名',
    description  VARCHAR(255) DEFAULT NULL            COMMENT '类型说明',
    sort_order   INT          NOT NULL DEFAULT 0      COMMENT '排序值',
    status       VARCHAR(20)  NOT NULL DEFAULT 'active' COMMENT '状态：active / disabled',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_types_key (type_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签类型表';

-- ============================================================
-- 2.2 统一标签表 (tags)
-- 说明：平台无关的统一标签字典，供展示、筛选、画像、推荐复用
-- ============================================================
CREATE TABLE tags (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '统一标签主键',
    tag_type_id     BIGINT       NOT NULL                COMMENT '标签类型 ID',
    tag_key         VARCHAR(100) NOT NULL                COMMENT '统一标签键',
    display_name    VARCHAR(100) NOT NULL                COMMENT '显示名称',
    alias_names     JSON         DEFAULT NULL            COMMENT '别名 JSON 列表',
    description     VARCHAR(255) DEFAULT NULL            COMMENT '标签说明',
    color           VARCHAR(30)  DEFAULT NULL            COMMENT '展示色值',
    icon            VARCHAR(50)  DEFAULT NULL            COMMENT '预留图标字段',
    parent_id       BIGINT       DEFAULT NULL            COMMENT '父标签 ID',
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

-- ============================================================
-- 2.3 平台标签映射表 (platform_tags)
-- 说明：保存平台原始标签并映射到统一标签
-- ============================================================
CREATE TABLE platform_tags (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '平台标签主键',
    oj_platform      VARCHAR(30)  NOT NULL                COMMENT '平台标识',
    source_tag_id    VARCHAR(100) DEFAULT NULL            COMMENT '平台原始标签 ID',
    source_slug      VARCHAR(150) DEFAULT NULL            COMMENT '平台原始 slug',
    source_name      VARCHAR(150) NOT NULL                COMMENT '平台原始标签名称',
    normalized_key   VARCHAR(100) DEFAULT NULL            COMMENT '归一化键',
    tag_type_id      BIGINT       DEFAULT NULL            COMMENT '推断标签类型 ID',
    tag_id           BIGINT       DEFAULT NULL            COMMENT '映射到统一标签 ID',
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

-- ============================================================
-- 2.4 题目标签关联表 (problem_tag_relations)
-- 说明：记录题目与统一标签 / 平台标签的多对多关系
-- ============================================================
CREATE TABLE problem_tag_relations (
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

-- ============================================================
-- 3. 提交记录表 (submissions)
-- 说明：记录用户每次代码提交的详细信息和判题结果
-- ============================================================
CREATE TABLE submissions (
    id                   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '提交记录唯一标识',
    user_id              BIGINT       NOT NULL                COMMENT '提交者用户 ID（关联 users.id）',
    problem_id           BIGINT       NOT NULL                COMMENT '题目本地 ID（关联 problems.id）',
    session_id           BIGINT       DEFAULT NULL            COMMENT '所属练习会话 ID（关联 practice_sessions.id），可为空（非会话内提交）',
    language             VARCHAR(20)  NOT NULL                COMMENT '编程语言（java / python3 / cpp 等）',
    code                 TEXT         NOT NULL                COMMENT '用户提交的源代码',
    status               VARCHAR(50)  NOT NULL DEFAULT 'Pending' COMMENT '判题结果状态（Pending / Accepted / Wrong Answer / Time Limit Exceeded 等）',
    runtime              VARCHAR(30)  DEFAULT NULL            COMMENT '运行耗时（如 "4 ms"）',
    memory               VARCHAR(30)  DEFAULT NULL            COMMENT '内存消耗（如 "39.2 MB"）',
    total_correct        INT          DEFAULT NULL            COMMENT '通过的测试用例数',
    total_testcases      INT          DEFAULT NULL            COMMENT '总测试用例数',
    remote_submission_id VARCHAR(50)  DEFAULT NULL            COMMENT '远程 OJ 返回的提交 ID（用于轮询结果）',
    submitted_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',

    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_problem_id (problem_id),
    INDEX idx_session_id (session_id),
    INDEX idx_submitted_at (submitted_at),
    CONSTRAINT fk_submissions_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_submissions_problem FOREIGN KEY (problem_id) REFERENCES problems(id),
    CONSTRAINT fk_submissions_session FOREIGN KEY (session_id) REFERENCES practice_sessions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提交记录表';

-- ============================================================
-- 4. 练习会话表 (practice_sessions)
-- 说明：每次从题库首页点击进入做题页面，创建一个新会话。
--       会话绑定一个 Dify 对话 ID，用于追踪整个学习路径。
-- ============================================================
CREATE TABLE practice_sessions (
    id                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '会话唯一标识',
    user_id               BIGINT       NOT NULL                COMMENT '用户 ID（关联 users.id）',
    dify_conversation_id  VARCHAR(100) DEFAULT NULL            COMMENT 'Dify 对话 ID（首次与 Dify 交互时生成）',
    started_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '会话开始时间',
    last_active_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后活跃时间（用于排序）',
    ended_at              DATETIME     DEFAULT NULL            COMMENT '会话结束时间（用户关闭页面或手动结束）',

    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_started_at (started_at),
    CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='练习会话表';

-- ============================================================
-- 5. 会话题目关联表 (session_problems)
-- 说明：记录每个会话中用户经历的题目轨迹。
--       jump_type 标记进入方式：initial（从题库首页进入）、
--       next_recommend（Dify 推荐跳转到下一题）。
--       通过 seq_order 保持题目在会话中的顺序。
-- ============================================================
CREATE TABLE session_problems (
    id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '关联记录唯一标识',
    session_id  BIGINT      NOT NULL                COMMENT '会话 ID（关联 practice_sessions.id）',
    problem_id  BIGINT      NOT NULL                COMMENT '题目 ID（关联 problems.id）',
    jump_type   VARCHAR(20) NOT NULL                COMMENT '跳转类型：initial（从题库进入）/ next_recommend（AI 推荐跳转）',
    seq_order   INT         NOT NULL DEFAULT 1      COMMENT '题目在会话中的顺序编号（从 1 开始递增）',
    jumped_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '跳转时间',

    PRIMARY KEY (id),
    INDEX idx_session_id (session_id),
    INDEX idx_problem_id (problem_id),
    CONSTRAINT fk_sp_session FOREIGN KEY (session_id) REFERENCES practice_sessions(id),
    CONSTRAINT fk_sp_problem FOREIGN KEY (problem_id) REFERENCES problems(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话题目关联表';

-- ============================================================
-- 6. 代码草稿表 (code_drafts)
-- 说明：保存用户在做题过程中的代码草稿，支持自动保存和跨设备恢复。
--       每个用户、每道题、每种语言只保留一份草稿（upsert 语义）。
-- ============================================================
CREATE TABLE code_drafts (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '草稿唯一标识',
    user_id      BIGINT       NOT NULL                COMMENT '用户 ID（关联 users.id）',
    problem_slug VARCHAR(200) NOT NULL                COMMENT '题目 slug（如 two-sum）',
    language     VARCHAR(20)  NOT NULL                COMMENT '编程语言（java / python3 / cpp）',
    code         TEXT         NOT NULL                COMMENT '用户草稿代码',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_problem_lang (user_id, problem_slug, language),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_drafts_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='代码草稿表';

-- ============================================================
-- 7. 用户 OJ 平台配置表 (user_oj_configs)
-- 说明：存储用户在各 OJ 平台的个人凭证（Cookie、CSRF Token 等），
--       每个用户每个平台只保留一份配置（upsert 语义）。
-- ============================================================
CREATE TABLE user_oj_configs (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '配置唯一标识',
    user_id      BIGINT       NOT NULL                COMMENT '用户 ID（关联 users.id）',
    oj_platform  VARCHAR(30)  NOT NULL                COMMENT 'OJ 平台标识（leetcode / luogu 等）',
    cookie_value TEXT         DEFAULT NULL            COMMENT 'OJ 平台 Cookie',
    csrf_token   VARCHAR(255) DEFAULT NULL            COMMENT 'CSRF Token',
    extra_config TEXT         DEFAULT NULL            COMMENT '额外配置（JSON 格式，预留扩展）',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_platform (user_id, oj_platform),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_ojconfig_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户 OJ 平台配置表';

-- ============================================================
-- 8. 题单表 (problem_sets)
-- 说明：存储用户或系统组好的一套题目集合。
--       支持三种来源：manual（手动选题）、quick（快速组题）、dify_smart（Dify 智能组题）
-- ============================================================
CREATE TABLE problem_sets (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '题单唯一标识',
    user_id          BIGINT       NOT NULL                COMMENT '创建者用户 ID（关联 users.id）',
    title            VARCHAR(200) NOT NULL                COMMENT '题单标题（如"动态规划入门10题"）',
    description      TEXT         DEFAULT NULL            COMMENT '题单描述',
    source_type      VARCHAR(30)  NOT NULL                COMMENT '来源类型：manual（手动选题）/ quick（快速组题）/ dify_smart（Dify 智能组题）',
    difficulty_level VARCHAR(30)  DEFAULT NULL            COMMENT '整体难度定位：beginner / intermediate / advanced / custom',
    problem_count    INT          NOT NULL DEFAULT 0      COMMENT '题目数量',
    total_score      INT          NOT NULL DEFAULT 0      COMMENT '总分（每题可设不同分值）',
    tags             JSON         DEFAULT NULL            COMMENT '标签/知识点 JSON 数组，如 ["dp","greedy","graph"]',
    dify_params      JSON         DEFAULT NULL            COMMENT 'Dify 智能组题时的输入参数快照（用于重新生成或回溯）',
    visibility       VARCHAR(20)  NOT NULL DEFAULT 'private' COMMENT '可见性：private（仅自己）/ public（公开）/ contest_only（仅比赛用）',
    status           VARCHAR(20)  NOT NULL DEFAULT 'draft'   COMMENT '状态：draft（草稿）/ published（已发布）/ archived（已归档）',
    oj_platform      VARCHAR(30)  NOT NULL DEFAULT 'leetcode' COMMENT 'OJ 平台标识',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_visibility (visibility),
    CONSTRAINT fk_pset_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题单表';

-- ============================================================
-- 9. 题单题目关联表 (problem_set_items)
-- 说明：题单内包含的题目列表，支持排序和单独设分
-- ============================================================
CREATE TABLE problem_set_items (
    id          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关联记录唯一标识',
    set_id      BIGINT   NOT NULL                COMMENT '题单 ID（关联 problem_sets.id）',
    problem_id  BIGINT   NOT NULL                COMMENT '题目 ID（关联 problems.id）',
    seq_order   INT      NOT NULL                COMMENT '题目在题单中的顺序（从 1 开始）',
    score       INT      NOT NULL DEFAULT 100    COMMENT '该题分值（默认 100）',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_set_problem (set_id, problem_id),
    INDEX idx_set_id (set_id),
    CONSTRAINT fk_psi_set FOREIGN KEY (set_id) REFERENCES problem_sets(id) ON DELETE CASCADE,
    CONSTRAINT fk_psi_problem FOREIGN KEY (problem_id) REFERENCES problems(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题单题目关联表';

-- ============================================================
-- 10. 用户画像表 (user_profiles)
-- 说明：记录用户的能力水平、做题偏好、薄弱项等，
--       用于智能组题时精准匹配难度和知识点
-- ============================================================
CREATE TABLE user_profiles (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '画像唯一标识',
    user_id          BIGINT       NOT NULL                COMMENT '用户 ID（关联 users.id）',
    skill_level      VARCHAR(30)  NOT NULL DEFAULT 'beginner' COMMENT '自评水平：beginner / intermediate / advanced / expert',
    target_level     VARCHAR(30)  DEFAULT NULL            COMMENT '目标水平',
    strong_tags      JSON         DEFAULT NULL            COMMENT '擅长领域 JSON 数组，如 ["array","string"]',
    weak_tags        JSON         DEFAULT NULL            COMMENT '薄弱领域 JSON 数组，如 ["dp","graph"]',
    solved_easy      INT          NOT NULL DEFAULT 0      COMMENT '已解决 Easy 题目数',
    solved_medium    INT          NOT NULL DEFAULT 0      COMMENT '已解决 Medium 题目数',
    solved_hard      INT          NOT NULL DEFAULT 0      COMMENT '已解决 Hard 题目数',
    total_submissions INT         NOT NULL DEFAULT 0      COMMENT '总提交次数',
    acceptance_rate  DECIMAL(5,2) NOT NULL DEFAULT 0      COMMENT '个人通过率（百分比）',
    last_analyzed_at DATETIME     DEFAULT NULL            COMMENT '上次画像分析时间',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    CONSTRAINT fk_up_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户画像表';

-- ============================================================
-- 11. 比赛表 (contests)
-- 说明：存储比赛的基本信息、规则配置和生命周期状态
-- ============================================================
CREATE TABLE contests (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '比赛唯一标识',
    creator_id       BIGINT       NOT NULL                COMMENT '创建者用户 ID（关联 users.id）',
    title            VARCHAR(200) NOT NULL                COMMENT '比赛标题',
    description      TEXT         DEFAULT NULL            COMMENT '比赛说明（规则、注意事项等）',
    contest_type     VARCHAR(20)  NOT NULL                COMMENT '比赛类型：individual（个人赛）/ team（组队赛）',
    status           VARCHAR(20)  NOT NULL DEFAULT 'draft' COMMENT '状态：draft / registering / running / frozen / ended / archived',
    problem_set_id   BIGINT       DEFAULT NULL            COMMENT '关联题单 ID（关联 problem_sets.id）',
    start_time       DATETIME     NOT NULL                COMMENT '比赛开始时间',
    end_time         DATETIME     NOT NULL                COMMENT '比赛结束时间',
    duration_minutes INT          NOT NULL                COMMENT '比赛时长（分钟）',
    freeze_minutes   INT          NOT NULL DEFAULT 0      COMMENT '封榜时间（比赛结束前 N 分钟封榜，0 = 不封榜）',
    max_participants INT          NOT NULL DEFAULT 0      COMMENT '最大参赛人数（0 = 不限）',
    max_team_size    INT          NOT NULL DEFAULT 3      COMMENT '组队赛最大队伍人数',
    scoring_rule     VARCHAR(30)  NOT NULL DEFAULT 'acm'  COMMENT '计分规则：acm（罚时制）/ oi（分数制）/ cf（Codeforces 风格）',
    penalty_time     INT          NOT NULL DEFAULT 20     COMMENT 'ACM 罚时：每次错误提交罚 N 分钟',
    allow_language   JSON         DEFAULT NULL            COMMENT '允许的编程语言 JSON 数组，null = 全部允许',
    is_public        TINYINT(1)   NOT NULL DEFAULT 1      COMMENT '是否公开（1=公开，所有人可见可报名）',
    password         VARCHAR(255) DEFAULT NULL            COMMENT '私有比赛密码（加密存储）',
    oj_platform      VARCHAR(30)  NOT NULL DEFAULT 'leetcode' COMMENT 'OJ 平台标识',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    CONSTRAINT fk_contest_creator FOREIGN KEY (creator_id) REFERENCES users(id),
    CONSTRAINT fk_contest_pset FOREIGN KEY (problem_set_id) REFERENCES problem_sets(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛表';

-- ============================================================
-- 12. 比赛报名表 (contest_registrations)
-- 说明：记录用户的比赛报名信息
-- ============================================================
CREATE TABLE contest_registrations (
    id            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '报名记录唯一标识',
    contest_id    BIGINT      NOT NULL                COMMENT '比赛 ID（关联 contests.id）',
    user_id       BIGINT      NOT NULL                COMMENT '用户 ID（关联 users.id）',
    team_id       BIGINT      DEFAULT NULL            COMMENT '队伍 ID（组队赛时关联 contest_teams.id）',
    status        VARCHAR(20) NOT NULL DEFAULT 'registered' COMMENT '状态：registered（已报名）/ cancelled（已取消）/ disqualified（已取消资格）',
    registered_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_contest_user (contest_id, user_id),
    INDEX idx_contest_id (contest_id),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_reg_contest FOREIGN KEY (contest_id) REFERENCES contests(id),
    CONSTRAINT fk_reg_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛报名表';

-- ============================================================
-- 13. 队伍表 (contest_teams)
-- 说明：组队赛时的队伍信息，每支队伍有唯一邀请码
-- ============================================================
CREATE TABLE contest_teams (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '队伍唯一标识',
    contest_id   BIGINT       NOT NULL                COMMENT '比赛 ID（关联 contests.id）',
    team_name    VARCHAR(100) NOT NULL                COMMENT '队伍名称',
    captain_id   BIGINT       NOT NULL                COMMENT '队长用户 ID（关联 users.id）',
    invite_code  VARCHAR(20)  NOT NULL                COMMENT '邀请码（其他人通过此码加入队伍）',
    member_count INT          NOT NULL DEFAULT 1      COMMENT '当前成员数',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_contest_team (contest_id, team_name),
    UNIQUE KEY uk_invite_code (invite_code),
    INDEX idx_contest_id (contest_id),
    CONSTRAINT fk_team_contest FOREIGN KEY (contest_id) REFERENCES contests(id),
    CONSTRAINT fk_team_captain FOREIGN KEY (captain_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛队伍表';

-- ============================================================
-- 14. 队伍成员表 (contest_team_members)
-- 说明：记录队伍中的每个成员
-- ============================================================
CREATE TABLE contest_team_members (
    id        BIGINT      NOT NULL AUTO_INCREMENT COMMENT '记录唯一标识',
    team_id   BIGINT      NOT NULL                COMMENT '队伍 ID（关联 contest_teams.id）',
    user_id   BIGINT      NOT NULL                COMMENT '用户 ID（关联 users.id）',
    role      VARCHAR(20) NOT NULL DEFAULT 'member' COMMENT '角色：captain（队长）/ member（队员）',
    joined_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_team_user (team_id, user_id),
    INDEX idx_team_id (team_id),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_tm_team FOREIGN KEY (team_id) REFERENCES contest_teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_tm_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='队伍成员表';

-- ============================================================
-- 15. 比赛提交表 (contest_submissions)
-- 说明：独立于普通提交的比赛专用提交记录，
--       支持按比赛维度统计和榜单计算
-- ============================================================
CREATE TABLE contest_submissions (
    id                   BIGINT      NOT NULL AUTO_INCREMENT COMMENT '比赛提交记录唯一标识',
    contest_id           BIGINT      NOT NULL                COMMENT '比赛 ID（关联 contests.id）',
    user_id              BIGINT      NOT NULL                COMMENT '提交者用户 ID（关联 users.id）',
    team_id              BIGINT      DEFAULT NULL            COMMENT '队伍 ID（组队赛时关联 contest_teams.id）',
    problem_id           BIGINT      NOT NULL                COMMENT '题目 ID（关联 problems.id）',
    language             VARCHAR(20) NOT NULL                COMMENT '编程语言',
    code                 TEXT        NOT NULL                COMMENT '用户提交的源代码',
    status               VARCHAR(50) NOT NULL DEFAULT 'Pending' COMMENT '判题状态（Pending / Accepted / Wrong Answer 等）',
    runtime              VARCHAR(30) DEFAULT NULL            COMMENT '运行耗时',
    memory               VARCHAR(30) DEFAULT NULL            COMMENT '内存消耗',
    total_correct        INT         DEFAULT NULL            COMMENT '通过的测试用例数',
    total_testcases      INT         DEFAULT NULL            COMMENT '总测试用例数',
    score                INT         NOT NULL DEFAULT 0      COMMENT 'OI 赛制的该次提交得分',
    remote_submission_id VARCHAR(50) DEFAULT NULL            COMMENT '远程 OJ 提交 ID',
    submitted_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',

    PRIMARY KEY (id),
    INDEX idx_contest_user (contest_id, user_id),
    INDEX idx_contest_problem (contest_id, problem_id),
    INDEX idx_contest_team (contest_id, team_id),
    INDEX idx_submitted_at (submitted_at),
    CONSTRAINT fk_csub_contest FOREIGN KEY (contest_id) REFERENCES contests(id),
    CONSTRAINT fk_csub_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_csub_problem FOREIGN KEY (problem_id) REFERENCES problems(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='比赛提交表';

-- ============================================================
-- 16. 榜单快照表 (contest_standings)
-- 说明：存储比赛榜单数据，支持封榜机制和历史回溯。
--       problem_details 存储每道题的详细作答情况（JSON 格式）
-- ============================================================
CREATE TABLE contest_standings (
    id              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '榜单记录唯一标识',
    contest_id      BIGINT      NOT NULL                COMMENT '比赛 ID（关联 contests.id）',
    user_id         BIGINT      DEFAULT NULL            COMMENT '用户 ID（个人赛时使用）',
    team_id         BIGINT      DEFAULT NULL            COMMENT '队伍 ID（组队赛时使用）',
    `rank`          INT         NOT NULL                COMMENT '排名',
    solved_count    INT         NOT NULL DEFAULT 0      COMMENT '解题数（ACM/CF 赛制）',
    total_score     INT         NOT NULL DEFAULT 0      COMMENT '总分（OI 赛制）',
    total_penalty   BIGINT      NOT NULL DEFAULT 0      COMMENT 'ACM 罚时（秒）',
    problem_details JSON        DEFAULT NULL            COMMENT '每题详情 JSON，如 [{"problemId":1,"accepted":true,"attempts":3,"firstAcTime":1200,"score":100}]',
    is_frozen       TINYINT(1)  NOT NULL DEFAULT 0      COMMENT '是否为封榜时刻的快照（1=是）',
    snapshot_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照时间',

    PRIMARY KEY (id),
    INDEX idx_contest_rank (contest_id, `rank`),
    INDEX idx_contest_user (contest_id, user_id),
    INDEX idx_contest_team (contest_id, team_id),
    CONSTRAINT fk_stand_contest FOREIGN KEY (contest_id) REFERENCES contests(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='榜单快照表';
