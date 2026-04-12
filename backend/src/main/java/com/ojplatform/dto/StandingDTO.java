package com.ojplatform.dto;

import java.util.List;

/**
 * 榜单响应 DTO
 * 包含榜单行列表 + 是否处于封榜状态
 */
public class StandingDTO {

    /** 比赛 ID */
    private Long contestId;

    /** 计分规则 */
    private String scoringRule;

    /** 是否已封榜 */
    private Boolean frozen;

    /** 题目列表（按顺序，用于表头显示） */
    private List<StandingProblem> problems;

    /** 榜单行列表（已按排名排序） */
    private List<StandingRow> rows;

    /**
     * 榜单中的题目信息
     */
    public static class StandingProblem {
        private Long problemId;
        private String slug;
        private String title;
        private String frontendId;
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
     * 榜单一行（一个用户/队伍的成绩）
     */
    public static class StandingRow {
        private Integer rank;
        private Long userId;
        private String username;
        private Long teamId;
        private String teamName;
        private Integer solvedCount;
        private Integer totalScore;
        private Long totalPenalty;
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
     * 某用户/队伍在某题上的作答结果
     */
    public static class ProblemResult {
        private Long problemId;
        private Boolean accepted;
        private Integer attempts;
        private Long firstAcTimeSeconds;
        private Integer score;
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
