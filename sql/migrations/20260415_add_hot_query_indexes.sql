-- Hot query indexes for submissions and contest-related read paths.
-- Run once on existing databases.

ALTER TABLE oj_platform.contest_registrations
    ADD INDEX idx_reg_contest_status_team (contest_id, status, team_id);

ALTER TABLE oj_platform.contest_registrations
    ADD INDEX idx_reg_user_status_contest (user_id, status, contest_id);

ALTER TABLE oj_platform.contest_submissions
    ADD INDEX idx_csub_contest_time (contest_id, submitted_at);

ALTER TABLE oj_platform.contest_submissions
    ADD INDEX idx_csub_contest_user_time (contest_id, user_id, submitted_at);

ALTER TABLE oj_platform.submissions
    ADD INDEX idx_sub_session_problem_status (session_id, problem_id, status);

ALTER TABLE oj_platform.submissions
    ADD INDEX idx_sub_user_problem_time (user_id, problem_id, submitted_at);

ALTER TABLE oj_platform.submissions
    ADD INDEX idx_sub_user_status_time (user_id, status, submitted_at);
