package com.ojplatform.dto;

import java.util.List;

/**
 * 榜单数据传输对象。
 */
public class StandingDTO {

    /**
     * 比赛ID。
     */
    private Long contestId;

    /**
     * 计分规则。
     */
    private String scoringRule;

    /**
     * 是否封榜隐藏。
     */
    private Boolean frozen;

    /**
     * 题目列表。
     */
    private List<StandingProblem> problems;

    /**
     * 榜单行列表。
     */
    private List<StandingRow> rows;

    /**
     * 榜单题目信息数据传输对象。
     */
    public static class StandingProblem {
        /**
         * 题目ID。
         */
        private Long problemId;
        /**
         * 题目标识。
         */
        private String slug;
        /**
         * 标题。
         */
        private String title;
        /**
         * 前端展示编号。
         */
        private String frontendId;
        /**
         * 分数。
         */
        private Integer score;

        public Long getProblemId() {
            return problemId;
        }

        public void setProblemId(Long problemId) {
            this.problemId = problemId;
        }

        public String getSlug() {
            return slug;
        }

        public void setSlug(String slug) {
            this.slug = slug;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getFrontendId() {
            return frontendId;
        }

        public void setFrontendId(String frontendId) {
            this.frontendId = frontendId;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }
    }

    /**
     * 榜单行数据传输对象。
     */
    public static class StandingRow {
        /**
         * 排名。
         */
        private Integer rank;
        /**
         * 用户ID。
         */
        private Long userId;
        /**
         * 用户名。
         */
        private String username;
        /**
         * 队伍ID。
         */
        private Long teamId;
        /**
         * 队伍名称。
         */
        private String teamName;
        /**
         * 通过题数。
         */
        private Integer solvedCount;
        /**
         * 总分。
         */
        private Integer totalScore;
        /**
         * 总罚时。
         */
        private Long totalPenalty;
        /**
         * 各题结果。
         */
        private List<ProblemResult> problemResults;

        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer rank) {
            this.rank = rank;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public Long getTeamId() {
            return teamId;
        }

        public void setTeamId(Long teamId) {
            this.teamId = teamId;
        }

        public String getTeamName() {
            return teamName;
        }

        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }

        public Integer getSolvedCount() {
            return solvedCount;
        }

        public void setSolvedCount(Integer solvedCount) {
            this.solvedCount = solvedCount;
        }

        public Integer getTotalScore() {
            return totalScore;
        }

        public void setTotalScore(Integer totalScore) {
            this.totalScore = totalScore;
        }

        public Long getTotalPenalty() {
            return totalPenalty;
        }

        public void setTotalPenalty(Long totalPenalty) {
            this.totalPenalty = totalPenalty;
        }

        public List<ProblemResult> getProblemResults() {
            return problemResults;
        }

        public void setProblemResults(List<ProblemResult> problemResults) {
            this.problemResults = problemResults;
        }
    }

    /**
     * 题目结果数据传输对象。
     */
    public static class ProblemResult {
        /**
         * 题目ID。
         */
        private Long problemId;
        /**
         * 是否通过。
         */
        private Boolean accepted;
        /**
         * 尝试次数。
         */
        private Integer attempts;
        /**
         * 首次通过时间（秒）。
         */
        private Long firstAcTimeSeconds;
        /**
         * 分数。
         */
        private Integer score;
        /**
         * 是否封榜隐藏。
         */
        private Boolean frozen;

        public Long getProblemId() {
            return problemId;
        }

        public void setProblemId(Long problemId) {
            this.problemId = problemId;
        }

        public Boolean getAccepted() {
            return accepted;
        }

        public void setAccepted(Boolean accepted) {
            this.accepted = accepted;
        }

        public Integer getAttempts() {
            return attempts;
        }

        public void setAttempts(Integer attempts) {
            this.attempts = attempts;
        }

        public Long getFirstAcTimeSeconds() {
            return firstAcTimeSeconds;
        }

        public void setFirstAcTimeSeconds(Long firstAcTimeSeconds) {
            this.firstAcTimeSeconds = firstAcTimeSeconds;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }

        public Boolean getFrozen() {
            return frozen;
        }

        public void setFrozen(Boolean frozen) {
            this.frozen = frozen;
        }
    }

    // ==================== Getter / Setter ====================

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    public String getScoringRule() {
        return scoringRule;
    }

    public void setScoringRule(String scoringRule) {
        this.scoringRule = scoringRule;
    }

    public Boolean getFrozen() {
        return frozen;
    }

    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

    public List<StandingProblem> getProblems() {
        return problems;
    }

    public void setProblems(List<StandingProblem> problems) {
        this.problems = problems;
    }

    public List<StandingRow> getRows() {
        return rows;
    }

    public void setRows(List<StandingRow> rows) {
        this.rows = rows;
    }
}
