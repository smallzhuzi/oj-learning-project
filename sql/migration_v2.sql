USE oj_platform;

-- 8. 题单表
CREATE TABLE IF NOT EXISTS problem_sets (
    id               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '题单唯一标识',
    user_id          BIGINT       NOT NULL,
    title            VARCHAR(200) NOT NULL,
    description      TEXT         DEFAULT NULL,
    source_type      VARCHAR(30)  NOT NULL,
    difficulty_level VARCHAR(30)  DEFAULT NULL,
    problem_count    INT          NOT NULL DEFAULT 0,
    total_score      INT          NOT NULL DEFAULT 0,
    tags             JSON         DEFAULT NULL,
    dify_params      JSON         DEFAULT NULL,
    visibility       VARCHAR(20)  NOT NULL DEFAULT 'private',
    status           VARCHAR(20)  NOT NULL DEFAULT 'draft',
    oj_platform      VARCHAR(30)  NOT NULL DEFAULT 'leetcode',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_user_id (user_id),
    CONSTRAINT fk_pset_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 题单题目关联表
CREATE TABLE IF NOT EXISTS problem_set_items (
    id          BIGINT   NOT NULL AUTO_INCREMENT,
    set_id      BIGINT   NOT NULL,
    problem_id  BIGINT   NOT NULL,
    seq_order   INT      NOT NULL,
    score       INT      NOT NULL DEFAULT 100,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_set_problem (set_id, problem_id),
    CONSTRAINT fk_psi_set FOREIGN KEY (set_id) REFERENCES problem_sets(id) ON DELETE CASCADE,
    CONSTRAINT fk_psi_problem FOREIGN KEY (problem_id) REFERENCES problems(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 用户画像表
CREATE TABLE IF NOT EXISTS user_profiles (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    skill_level      VARCHAR(30)  NOT NULL DEFAULT 'beginner',
    target_level     VARCHAR(30)  DEFAULT NULL,
    strong_tags      JSON         DEFAULT NULL,
    weak_tags        JSON         DEFAULT NULL,
    solved_easy      INT          NOT NULL DEFAULT 0,
    solved_medium    INT          NOT NULL DEFAULT 0,
    solved_hard      INT          NOT NULL DEFAULT 0,
    total_submissions INT         NOT NULL DEFAULT 0,
    acceptance_rate  DECIMAL(5,2) NOT NULL DEFAULT 0,
    last_analyzed_at DATETIME     DEFAULT NULL,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_id (user_id),
    CONSTRAINT fk_up_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. 比赛表
CREATE TABLE IF NOT EXISTS contests (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    creator_id       BIGINT       NOT NULL,
    title            VARCHAR(200) NOT NULL,
    description      TEXT         DEFAULT NULL,
    contest_type     VARCHAR(20)  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'draft',
    problem_set_id   BIGINT       DEFAULT NULL,
    start_time       DATETIME     NOT NULL,
    end_time         DATETIME     NOT NULL,
    duration_minutes INT          NOT NULL,
    freeze_minutes   INT          NOT NULL DEFAULT 0,
    max_participants INT          NOT NULL DEFAULT 0,
    max_team_size    INT          NOT NULL DEFAULT 3,
    scoring_rule     VARCHAR(30)  NOT NULL DEFAULT 'acm',
    penalty_time     INT          NOT NULL DEFAULT 20,
    allow_language   JSON         DEFAULT NULL,
    is_public        TINYINT(1)   NOT NULL DEFAULT 1,
    password         VARCHAR(255) DEFAULT NULL,
    oj_platform      VARCHAR(30)  NOT NULL DEFAULT 'leetcode',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_status (status),
    CONSTRAINT fk_contest_creator FOREIGN KEY (creator_id) REFERENCES users(id),
    CONSTRAINT fk_contest_pset FOREIGN KEY (problem_set_id) REFERENCES problem_sets(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. 比赛报名表
CREATE TABLE IF NOT EXISTS contest_registrations (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    contest_id    BIGINT      NOT NULL,
    user_id       BIGINT      NOT NULL,
    team_id       BIGINT      DEFAULT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'registered',
    registered_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_contest_user (contest_id, user_id),
    CONSTRAINT fk_reg_contest FOREIGN KEY (contest_id) REFERENCES contests(id),
    CONSTRAINT fk_reg_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. 队伍表
CREATE TABLE IF NOT EXISTS contest_teams (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    contest_id   BIGINT       NOT NULL,
    team_name    VARCHAR(100) NOT NULL,
    captain_id   BIGINT       NOT NULL,
    invite_code  VARCHAR(20)  NOT NULL,
    member_count INT          NOT NULL DEFAULT 1,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_contest_team (contest_id, team_name),
    UNIQUE KEY uk_invite_code (invite_code),
    CONSTRAINT fk_team_contest FOREIGN KEY (contest_id) REFERENCES contests(id),
    CONSTRAINT fk_team_captain FOREIGN KEY (captain_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. 队伍成员表
CREATE TABLE IF NOT EXISTS contest_team_members (
    id        BIGINT      NOT NULL AUTO_INCREMENT,
    team_id   BIGINT      NOT NULL,
    user_id   BIGINT      NOT NULL,
    role      VARCHAR(20) NOT NULL DEFAULT 'member',
    joined_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_team_user (team_id, user_id),
    CONSTRAINT fk_tm_team FOREIGN KEY (team_id) REFERENCES contest_teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_tm_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. 比赛提交表
CREATE TABLE IF NOT EXISTS contest_submissions (
    id                   BIGINT      NOT NULL AUTO_INCREMENT,
    contest_id           BIGINT      NOT NULL,
    user_id              BIGINT      NOT NULL,
    team_id              BIGINT      DEFAULT NULL,
    problem_id           BIGINT      NOT NULL,
    language             VARCHAR(20) NOT NULL,
    code                 TEXT        NOT NULL,
    status               VARCHAR(50) NOT NULL DEFAULT 'Pending',
    runtime              VARCHAR(30) DEFAULT NULL,
    memory               VARCHAR(30) DEFAULT NULL,
    total_correct        INT         DEFAULT NULL,
    total_testcases      INT         DEFAULT NULL,
    score                INT         NOT NULL DEFAULT 0,
    remote_submission_id VARCHAR(50) DEFAULT NULL,
    submitted_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_contest_user (contest_id, user_id),
    INDEX idx_contest_problem (contest_id, problem_id),
    CONSTRAINT fk_csub_contest FOREIGN KEY (contest_id) REFERENCES contests(id),
    CONSTRAINT fk_csub_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_csub_problem FOREIGN KEY (problem_id) REFERENCES problems(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. 榜单快照表
CREATE TABLE IF NOT EXISTS contest_standings (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    contest_id      BIGINT      NOT NULL,
    user_id         BIGINT      DEFAULT NULL,
    team_id         BIGINT      DEFAULT NULL,
    `rank`          INT         NOT NULL,
    solved_count    INT         NOT NULL DEFAULT 0,
    total_score     INT         NOT NULL DEFAULT 0,
    total_penalty   BIGINT      NOT NULL DEFAULT 0,
    problem_details JSON        DEFAULT NULL,
    is_frozen       TINYINT(1)  NOT NULL DEFAULT 0,
    snapshot_time   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_contest_rank (contest_id, `rank`),
    CONSTRAINT fk_stand_contest FOREIGN KEY (contest_id) REFERENCES contests(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
