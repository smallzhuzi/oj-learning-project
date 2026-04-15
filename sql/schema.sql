create table oj_platform.code_drafts
(
    id           bigint auto_increment comment '草稿唯一标识'
        primary key,
    user_id      bigint                             not null comment '用户 ID（关联 users.id）',
    problem_slug varchar(200)                       not null comment '题目 slug（如 two-sum）',
    language     varchar(20)                        not null comment '编程语言（java / python3 / cpp）',
    code         text                               not null comment '用户草稿代码',
    created_at   datetime default CURRENT_TIMESTAMP not null comment '首次创建时间',
    updated_at   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '最后更新时间',
    constraint uk_user_problem_lang
        unique (user_id, problem_slug, language)
)
    comment '代码草稿表';

create index idx_user_id
    on oj_platform.code_drafts (user_id);

create table oj_platform.contest_team_participants
(
    id         bigint auto_increment
        primary key,
    contest_id bigint                             not null comment '比赛ID',
    team_id    bigint                             not null comment '队伍ID',
    user_id    bigint                             not null comment '出场成员用户ID',
    created_at datetime default CURRENT_TIMESTAMP null,
    constraint uk_contest_team_user
        unique (contest_id, team_id, user_id)
)
    comment '比赛队伍出场成员表' charset = utf8mb4;

create index idx_contest_id
    on oj_platform.contest_team_participants (contest_id);

create index idx_team_id
    on oj_platform.contest_team_participants (team_id);

create table oj_platform.platform_tags
(
    id             bigint auto_increment comment '平台标签主键'
        primary key,
    oj_platform    varchar(30)                        not null comment '平台标识，如 leetcode / luogu',
    source_tag_id  varchar(100)                       null comment '平台原始标签 ID，如 洛谷数字 ID',
    source_slug    varchar(150)                       null comment '平台原始 slug，如 dynamic-programming',
    source_name    varchar(150)                       not null comment '平台原始标签名，如 动态规划',
    normalized_key varchar(100)                       null comment '归一化键，便于去重匹配',
    tag_type_id    bigint                             null comment '平台标签推断类型',
    tag_id         bigint                             null comment '映射到统一标签 ID，可为空表示待人工映射',
    metadata       json                               null comment '扩展元数据',
    created_at     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at     datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_platform_tag_source
        unique (oj_platform, source_tag_id, source_slug, source_name)
)
    comment '平台标签映射表';

create index fk_platform_tags_type
    on oj_platform.platform_tags (tag_type_id);

create index idx_platform_tags_key
    on oj_platform.platform_tags (normalized_key);

create index idx_platform_tags_tag_id
    on oj_platform.platform_tags (tag_id);

create table oj_platform.practice_sessions
(
    id                   bigint auto_increment comment '会话唯一标识'
        primary key,
    user_id              bigint                             not null comment '用户 ID（关联 users.id）',
    dify_conversation_id varchar(100)                       null comment 'Dify 对话 ID（首次与 Dify 交互时生成）',
    started_at           datetime default CURRENT_TIMESTAMP not null comment '会话开始时间',
    last_active_at       datetime default CURRENT_TIMESTAMP not null comment '最后活跃时间',
    ended_at             datetime                           null comment '会话结束时间（用户关闭页面或手动结束）'
)
    comment '练习会话表';

create index idx_started_at
    on oj_platform.practice_sessions (started_at);

create index idx_user_id
    on oj_platform.practice_sessions (user_id);

create table oj_platform.problems
(
    id               bigint auto_increment comment '题目本地唯一标识'
        primary key,
    slug             varchar(200)                          not null comment '题目 slug（如 two-sum），用于构造 URL 和 API 调用',
    title            varchar(200)                          not null comment '题目标题（如"两数之和"）',
    difficulty       varchar(50)                           not null,
    acceptance_rate  decimal(5, 2)                         null comment '通过率（百分比，如 49.50 表示 49.50%）',
    oj_platform      varchar(30) default 'leetcode'        not null comment 'OJ 平台标识（leetcode / codeforces 等，当前仅 leetcode）',
    content_markdown text                                  null comment '题目描述（Markdown/HTML），从远程拉取后缓存',
    code_snippets    json                                  null comment '各语言初始代码模板 JSON 数组',
    topic_tags       text                                  null comment '题目标签 JSON 数组',
    frontend_id      varchar(20)                           null comment 'LeetCode 前端展示的题号（如 "1"、"2"）',
    question_id      varchar(20)                           null comment 'LeetCode 内部题目 ID（提交代码时需要）',
    created_at       datetime    default CURRENT_TIMESTAMP not null comment '首次入库时间',
    updated_at       datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '最后更新时间',
    constraint uk_slug_platform
        unique (slug, oj_platform)
)
    comment '题目缓存表';

create index idx_difficulty
    on oj_platform.problems (difficulty);

create index idx_frontend_id
    on oj_platform.problems (frontend_id);

create table oj_platform.session_problems
(
    id         bigint auto_increment comment '关联记录唯一标识'
        primary key,
    session_id bigint                             not null comment '会话 ID（关联 practice_sessions.id）',
    problem_id bigint                             not null comment '题目 ID（关联 problems.id）',
    jump_type  varchar(20)                        not null comment '跳转类型：initial（从题库进入）/ next_recommend（AI 推荐跳转）',
    seq_order  int      default 1                 not null comment '题目在会话中的顺序编号（从 1 开始递增）',
    jumped_at  datetime default CURRENT_TIMESTAMP not null comment '跳转时间'
)
    comment '会话题目关联表';

create index idx_problem_id
    on oj_platform.session_problems (problem_id);

create index idx_session_id
    on oj_platform.session_problems (session_id);

create table oj_platform.submissions
(
    id                   bigint auto_increment comment '提交记录唯一标识'
        primary key,
    user_id              bigint                                not null comment '提交者用户 ID（关联 users.id）',
    problem_id           bigint                                not null comment '题目本地 ID（关联 problems.id）',
    session_id           bigint                                null comment '所属练习会话 ID（关联 practice_sessions.id），可为空（非会话内提交）',
    language             varchar(20)                           not null comment '编程语言（java / python3 / cpp 等）',
    code                 text                                  not null comment '用户提交的源代码',
    status               varchar(50) default 'Pending'         not null comment '判题结果状态（Pending / Accepted / Wrong Answer / Time Limit Exceeded 等）',
    runtime              varchar(30)                           null comment '运行耗时（如 "4 ms"）',
    memory               varchar(30)                           null comment '内存消耗（如 "39.2 MB"）',
    total_correct        int                                   null comment '通过的测试用例数',
    total_testcases      int                                   null comment '总测试用例数',
    remote_submission_id varchar(50)                           null comment '远程 OJ 返回的提交 ID（用于轮询结果）',
    submitted_at         datetime    default CURRENT_TIMESTAMP not null comment '提交时间'
)
    comment '提交记录表';

create index idx_problem_id
    on oj_platform.submissions (problem_id);

create index idx_session_id
    on oj_platform.submissions (session_id);

create index idx_submitted_at
    on oj_platform.submissions (submitted_at);

create index idx_user_id
    on oj_platform.submissions (user_id);

create index idx_sub_session_problem_status
    on oj_platform.submissions (session_id, problem_id, status);

create index idx_sub_user_problem_time
    on oj_platform.submissions (user_id, problem_id, submitted_at);

create index idx_sub_user_status_time
    on oj_platform.submissions (user_id, status, submitted_at);

create table oj_platform.tag_types
(
    id          bigint auto_increment comment '标签类型主键'
        primary key,
    type_key    varchar(50)                           not null comment '类型键，如 algorithm / data_structure / source',
    type_name   varchar(100)                          not null comment '类型显示名，如 算法 / 数据结构 / 来源',
    description varchar(255)                          null comment '类型说明',
    sort_order  int         default 0                 not null comment '排序值',
    status      varchar(20) default 'active'          not null comment '状态：active / disabled',
    created_at  datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at  datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_tag_types_key
        unique (type_key)
)
    comment '标签类型表';

create table oj_platform.tags
(
    id           bigint auto_increment comment '统一标签主键'
        primary key,
    tag_type_id  bigint                                not null comment '标签类型 ID',
    tag_key      varchar(100)                          not null comment '统一标签键，如 dp / array / luogu-noip',
    display_name varchar(100)                          not null comment '显示名称，如 动态规划 / 数组 / 洛谷 NOIP',
    alias_names  json                                  null comment '别名列表 JSON',
    description  varchar(255)                          null comment '标签说明',
    color        varchar(30)                           null comment '展示色值，如 #3b82f6',
    icon         varchar(50)                           null comment '预留图标字段',
    parent_id    bigint                                null comment '父标签 ID，支持层级关系',
    sort_order   int         default 0                 not null comment '排序值',
    status       varchar(20) default 'active'          not null comment '状态：active / disabled',
    created_at   datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at   datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_tags_type_key
        unique (tag_type_id, tag_key)
)
    comment '统一标签字典表';

create table oj_platform.problem_tag_relations
(
    id              bigint auto_increment comment '关系主键'
        primary key,
    problem_id      bigint                             not null comment '题目 ID',
    tag_id          bigint                             null comment '统一标签 ID',
    platform_tag_id bigint                             null comment '平台标签 ID',
    created_at      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_problem_tag_relation
        unique (problem_id, tag_id, platform_tag_id),
    constraint fk_ptr_platform_tag
        foreign key (platform_tag_id) references oj_platform.platform_tags (id)
            on delete cascade,
    constraint fk_ptr_problem
        foreign key (problem_id) references oj_platform.problems (id)
            on delete cascade,
    constraint fk_ptr_tag
        foreign key (tag_id) references oj_platform.tags (id)
            on delete cascade
)
    comment '题目标签关联表';

create index idx_ptr_platform_tag_id
    on oj_platform.problem_tag_relations (platform_tag_id);

create index idx_ptr_problem_id
    on oj_platform.problem_tag_relations (problem_id);

create index idx_ptr_tag_id
    on oj_platform.problem_tag_relations (tag_id);

create index idx_tags_parent_id
    on oj_platform.tags (parent_id);

create table oj_platform.team_join_requests
(
    id         bigint auto_increment
        primary key,
    team_id    bigint                                not null comment '队伍ID',
    user_id    bigint                                not null comment '申请用户ID',
    message    varchar(200)                          null comment '申请留言',
    status     varchar(20) default 'pending'         null comment '状态：pending/approved/rejected',
    created_at datetime    default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '入队申请表';

create table oj_platform.team_members
(
    id        bigint auto_increment
        primary key,
    team_id   bigint                                not null comment '队伍ID',
    user_id   bigint                                not null comment '用户ID',
    role      varchar(20) default 'member'          null comment '角色：captain/member',
    joined_at datetime    default CURRENT_TIMESTAMP null comment '加入时间',
    constraint uk_team_user
        unique (team_id, user_id)
)
    comment '队伍成员表';

create table oj_platform.teams
(
    id           bigint auto_increment
        primary key,
    team_name    varchar(100)                       not null comment '队伍名称',
    description  varchar(500)                       null comment '队伍描述',
    captain_id   bigint                             not null comment '队长用户ID',
    invite_code  varchar(16)                        not null comment '邀请码',
    member_count int      default 1                 null comment '当前成员数',
    created_at   datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '独立队伍表';

create table oj_platform.user_oj_configs
(
    id           bigint auto_increment
        primary key,
    user_id      bigint                             not null,
    oj_platform  varchar(30)                        not null,
    cookie_value text                               null,
    csrf_token   varchar(255)                       null,
    extra_config text                               null,
    created_at   datetime default CURRENT_TIMESTAMP not null,
    updated_at   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_user_platform
        unique (user_id, oj_platform)
);

create table oj_platform.users
(
    id         bigint auto_increment comment '用户唯一标识'
        primary key,
    username   varchar(50)                           not null comment '用户名（登录名，不可重复）',
    email      varchar(100)                          not null comment '用户邮箱（不可重复，用于找回密码等）',
    password   varchar(255)                          not null comment '加密后的密码（BCrypt）',
    role       varchar(20) default 'user'            not null,
    status     varchar(20) default 'active'          not null,
    created_at datetime    default CURRENT_TIMESTAMP not null comment '注册时间',
    constraint uk_email
        unique (email),
    constraint uk_username
        unique (username)
)
    comment '用户表';

create table oj_platform.problem_sets
(
    id               bigint auto_increment comment '棰樺崟鍞?竴鏍囪瘑'
        primary key,
    user_id          bigint                                not null,
    title            varchar(200)                          not null,
    description      text                                  null,
    source_type      varchar(30)                           not null,
    difficulty_level varchar(30)                           null,
    problem_count    int         default 0                 not null,
    total_score      int         default 0                 not null,
    tags             json                                  null,
    dify_params      json                                  null,
    visibility       varchar(20) default 'private'         not null,
    status           varchar(20) default 'draft'           not null,
    oj_platform      varchar(30) default 'leetcode'        not null,
    created_at       datetime    default CURRENT_TIMESTAMP not null,
    updated_at       datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint fk_pset_user
        foreign key (user_id) references oj_platform.users (id)
);

create table oj_platform.contests
(
    id               bigint auto_increment
        primary key,
    creator_id       bigint                                not null,
    title            varchar(200)                          not null,
    description      text                                  null,
    contest_type     varchar(20)                           not null,
    status           varchar(20) default 'draft'           not null,
    problem_set_id   bigint                                null,
    start_time       datetime                              not null,
    end_time         datetime                              not null,
    duration_minutes int                                   not null,
    freeze_minutes   int         default 0                 not null,
    max_participants int         default 0                 not null,
    max_team_size    int         default 3                 not null,
    min_team_size    int         default 1                 null comment '组队赛最少队伍人数',
    scoring_rule     varchar(30) default 'acm'             not null,
    penalty_time     int         default 20                not null,
    allow_language   json                                  null,
    is_public        tinyint(1)  default 1                 not null,
    password         varchar(255)                          null,
    oj_platform      varchar(30) default 'leetcode'        not null,
    draft_problems   text                                  null comment '草稿阶段暂存的题目列表
  JSON（发布后清空）',
    created_at       datetime    default CURRENT_TIMESTAMP not null,
    updated_at       datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint fk_contest_creator
        foreign key (creator_id) references oj_platform.users (id),
    constraint fk_contest_pset
        foreign key (problem_set_id) references oj_platform.problem_sets (id)
);

create table oj_platform.contest_registrations
(
    id            bigint auto_increment
        primary key,
    contest_id    bigint                                not null,
    user_id       bigint                                not null,
    team_id       bigint                                null,
    status        varchar(20) default 'registered'      not null,
    registered_at datetime    default CURRENT_TIMESTAMP not null,
    constraint uk_contest_user
        unique (contest_id, user_id),
    constraint fk_reg_contest
        foreign key (contest_id) references oj_platform.contests (id),
    constraint fk_reg_user
        foreign key (user_id) references oj_platform.users (id)
);

create index idx_reg_contest_status_team
    on oj_platform.contest_registrations (contest_id, status, team_id);

create index idx_reg_user_status_contest
    on oj_platform.contest_registrations (user_id, status, contest_id);

create table oj_platform.contest_standings
(
    id              bigint auto_increment
        primary key,
    contest_id      bigint                               not null,
    user_id         bigint                               null,
    team_id         bigint                               null,
    `rank`          int                                  not null,
    solved_count    int        default 0                 not null,
    total_score     int        default 0                 not null,
    total_penalty   bigint     default 0                 not null,
    problem_details json                                 null,
    is_frozen       tinyint(1) default 0                 not null,
    snapshot_time   datetime   default CURRENT_TIMESTAMP not null,
    constraint uk_standing_contest_user_frozen
        unique (contest_id, user_id, is_frozen),
    constraint uk_standing_contest_team_frozen
        unique (contest_id, team_id, is_frozen),
    constraint fk_stand_contest
        foreign key (contest_id) references oj_platform.contests (id)
);

create index idx_contest_rank
    on oj_platform.contest_standings (contest_id, `rank`);

create table oj_platform.contest_submissions
(
    id                   bigint auto_increment
        primary key,
    contest_id           bigint                                not null,
    user_id              bigint                                not null,
    team_id              bigint                                null,
    problem_id           bigint                                not null,
    language             varchar(20)                           not null,
    code                 text                                  not null,
    status               varchar(50) default 'Pending'         not null,
    runtime              varchar(30)                           null,
    memory               varchar(30)                           null,
    total_correct        int                                   null,
    total_testcases      int                                   null,
    score                int         default 0                 not null,
    remote_submission_id varchar(50)                           null,
    submitted_at         datetime    default CURRENT_TIMESTAMP not null,
    constraint fk_csub_contest
        foreign key (contest_id) references oj_platform.contests (id),
    constraint fk_csub_problem
        foreign key (problem_id) references oj_platform.problems (id),
    constraint fk_csub_user
        foreign key (user_id) references oj_platform.users (id)
);

create index idx_contest_problem
    on oj_platform.contest_submissions (contest_id, problem_id);

create index idx_contest_user
    on oj_platform.contest_submissions (contest_id, user_id);

create index idx_csub_contest_time
    on oj_platform.contest_submissions (contest_id, submitted_at);

create index idx_csub_contest_user_time
    on oj_platform.contest_submissions (contest_id, user_id, submitted_at);

create table oj_platform.contest_teams
(
    id           bigint auto_increment
        primary key,
    contest_id   bigint                             not null,
    team_name    varchar(100)                       not null,
    description  varchar(500)                       null comment '队伍描述',
    captain_id   bigint                             not null,
    invite_code  varchar(20)                        not null,
    member_count int      default 1                 not null,
    created_at   datetime default CURRENT_TIMESTAMP not null,
    constraint uk_contest_team
        unique (contest_id, team_name),
    constraint uk_invite_code
        unique (invite_code),
    constraint fk_team_captain
        foreign key (captain_id) references oj_platform.users (id),
    constraint fk_team_contest
        foreign key (contest_id) references oj_platform.contests (id)
);

create table oj_platform.contest_team_members
(
    id        bigint auto_increment
        primary key,
    team_id   bigint                                not null,
    user_id   bigint                                not null,
    role      varchar(20) default 'member'          not null,
    joined_at datetime    default CURRENT_TIMESTAMP not null,
    constraint uk_team_user
        unique (team_id, user_id),
    constraint fk_tm_team
        foreign key (team_id) references oj_platform.contest_teams (id)
            on delete cascade,
    constraint fk_tm_user
        foreign key (user_id) references oj_platform.users (id)
);

create index idx_creator_id
    on oj_platform.contests (creator_id);

create index idx_status
    on oj_platform.contests (status);

create table oj_platform.problem_set_items
(
    id         bigint auto_increment
        primary key,
    set_id     bigint                             not null,
    problem_id bigint                             not null,
    seq_order  int                                not null,
    score      int      default 100               not null,
    created_at datetime default CURRENT_TIMESTAMP not null,
    constraint uk_set_problem
        unique (set_id, problem_id),
    constraint fk_psi_problem
        foreign key (problem_id) references oj_platform.problems (id),
    constraint fk_psi_set
        foreign key (set_id) references oj_platform.problem_sets (id)
            on delete cascade
);

create index idx_user_id
    on oj_platform.problem_sets (user_id);

create table oj_platform.user_profiles
(
    id                bigint auto_increment
        primary key,
    user_id           bigint                                  not null,
    skill_level       varchar(30)   default 'beginner'        not null,
    target_level      varchar(30)                             null,
    strong_tags       json                                    null,
    weak_tags         json                                    null,
    solved_easy       int           default 0                 not null,
    solved_medium     int           default 0                 not null,
    solved_hard       int           default 0                 not null,
    total_submissions int           default 0                 not null,
    acceptance_rate   decimal(5, 2) default 0.00              not null,
    last_analyzed_at  datetime                                null,
    created_at        datetime      default CURRENT_TIMESTAMP not null,
    updated_at        datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    constraint uk_user_id
        unique (user_id),
    constraint fk_up_user
        foreign key (user_id) references oj_platform.users (id)
);
