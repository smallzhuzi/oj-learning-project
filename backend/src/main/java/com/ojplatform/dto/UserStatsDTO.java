package com.ojplatform.dto;

import java.util.List;
import java.util.Map;

/**
 * 用户做题统计 DTO
 * 包含总计、按平台、按难度、近期每日提交数据
 */
public class UserStatsDTO {

    /** 总计统计 */
    private StatSummary total;

    /** 按平台统计 (key: leetcode / luogu) */
    private Map<String, StatSummary> platforms;

    /** 按平台+难度统计 (key: 平台, value: {难度 → 解题数}) */
    private Map<String, Map<String, Integer>> difficulties;

    /** 近 30 天每日提交次数 */
    private List<DailyCount> recentDaily;

    // ==================== 内部类 ====================

    public static class StatSummary {
        private int solved;
        private int submitted;
        private double acceptanceRate;

        public StatSummary() {}
        public StatSummary(int solved, int submitted, double acceptanceRate) {
            this.solved = solved;
            this.submitted = submitted;
            this.acceptanceRate = acceptanceRate;
        }

        public int getSolved() { return solved; }
        public void setSolved(int solved) { this.solved = solved; }
        public int getSubmitted() { return submitted; }
        public void setSubmitted(int submitted) { this.submitted = submitted; }
        public double getAcceptanceRate() { return acceptanceRate; }
        public void setAcceptanceRate(double acceptanceRate) { this.acceptanceRate = acceptanceRate; }
    }

    public static class DailyCount {
        private String date;
        private int count;

        public DailyCount() {}
        public DailyCount(String date, int count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    // ==================== Getter / Setter ====================

    public StatSummary getTotal() { return total; }
    public void setTotal(StatSummary total) { this.total = total; }
    public Map<String, StatSummary> getPlatforms() { return platforms; }
    public void setPlatforms(Map<String, StatSummary> platforms) { this.platforms = platforms; }
    public Map<String, Map<String, Integer>> getDifficulties() { return difficulties; }
    public void setDifficulties(Map<String, Map<String, Integer>> difficulties) { this.difficulties = difficulties; }
    public List<DailyCount> getRecentDaily() { return recentDaily; }
    public void setRecentDaily(List<DailyCount> recentDaily) { this.recentDaily = recentDaily; }
}
