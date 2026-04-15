package com.ojplatform.dto;

import java.util.List;
import java.util.Map;

/**
 * 用户统计数据传输对象。
 */
public class UserStatsDTO {

    /**
     * 总数。
     */
    private StatSummary total;

    /**
     * 平台统计。
     */
    private Map<String, StatSummary> platforms;

    /**
     * 难度统计。
     */
    private Map<String, Map<String, Integer>> difficulties;

    /**
     * 最近每日统计。
     */
    private List<DailyCount> recentDaily;

    // ==================== 内部类 ====================

    /**
     * 统计汇总数据传输对象。
     */
    public static class StatSummary {
        /**
         * 已通过数量。
         */
        private int solved;
        /**
         * 已提交数量。
         */
        private int submitted;
        /**
         * 通过率。
         */
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

    /**
     * 每日数量数据传输对象。
     */
    public static class DailyCount {
        /**
         * 日期。
         */
        private String date;
        /**
         * 数量。
         */
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
