-- Deduplicate contest standings before adding uniqueness constraints.
-- Keep the latest row for the same contest participant and frozen view.

DELETE s1
FROM oj_platform.contest_standings s1
JOIN oj_platform.contest_standings s2
  ON s1.contest_id = s2.contest_id
 AND IFNULL(s1.user_id, -1) = IFNULL(s2.user_id, -1)
 AND IFNULL(s1.team_id, -1) = IFNULL(s2.team_id, -1)
 AND s1.is_frozen = s2.is_frozen
 AND s1.id < s2.id;

ALTER TABLE oj_platform.contest_standings
    ADD CONSTRAINT uk_standing_contest_user_frozen
        UNIQUE (contest_id, user_id, is_frozen);

ALTER TABLE oj_platform.contest_standings
    ADD CONSTRAINT uk_standing_contest_team_frozen
        UNIQUE (contest_id, team_id, is_frozen);
