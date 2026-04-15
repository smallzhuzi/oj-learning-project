package com.ojplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 判题队列属性配置类。
 */
@Configuration
@ConfigurationProperties(prefix = "judge.queue")
public class JudgeQueueProperties {

    private String exchange = "judge.direct";
    private String contestSubmitQueue = "judge.contest.submit.queue";
    private String contestSubmitRoutingKey = "judge.contest.submit";
    private String contestSubmitDlq = "judge.contest.submit.dlq";
    private String contestSubmitDlRoutingKey = "judge.contest.submit.dlq";
    private String contestPollQueue = "judge.contest.poll.queue";
    private String contestPollRoutingKey = "judge.contest.poll";
    private String contestPollDlq = "judge.contest.poll.dlq";
    private String contestPollDlRoutingKey = "judge.contest.poll.dlq";
    private String contestPollDelayQueue = "judge.contest.poll.delay.queue";
    private String contestPollDelayRoutingKey = "judge.contest.poll.delay";
    private long initialPollDelayMs = 2000L;
    private long pollDelayMs = 2000L;
    private long maxPollDelayMs = 10000L;
    private int maxPollAttempts = 180;
    private long messageDedupTtlMs = 21600000L;
    private long compensationFixedDelayMs = 60000L;
    private long submitCompensationDelaySeconds = 30L;
    private long pollCompensationDelaySeconds = 60L;
    private long compensationThrottleSeconds = 120L;
    private int compensationBatchSize = 50;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getContestSubmitQueue() {
        return contestSubmitQueue;
    }

    public void setContestSubmitQueue(String contestSubmitQueue) {
        this.contestSubmitQueue = contestSubmitQueue;
    }

    public String getContestSubmitRoutingKey() {
        return contestSubmitRoutingKey;
    }

    public void setContestSubmitRoutingKey(String contestSubmitRoutingKey) {
        this.contestSubmitRoutingKey = contestSubmitRoutingKey;
    }

    public String getContestSubmitDlq() {
        return contestSubmitDlq;
    }

    public void setContestSubmitDlq(String contestSubmitDlq) {
        this.contestSubmitDlq = contestSubmitDlq;
    }

    public String getContestSubmitDlRoutingKey() {
        return contestSubmitDlRoutingKey;
    }

    public void setContestSubmitDlRoutingKey(String contestSubmitDlRoutingKey) {
        this.contestSubmitDlRoutingKey = contestSubmitDlRoutingKey;
    }

    public String getContestPollQueue() {
        return contestPollQueue;
    }

    public void setContestPollQueue(String contestPollQueue) {
        this.contestPollQueue = contestPollQueue;
    }

    public String getContestPollRoutingKey() {
        return contestPollRoutingKey;
    }

    public void setContestPollRoutingKey(String contestPollRoutingKey) {
        this.contestPollRoutingKey = contestPollRoutingKey;
    }

    public String getContestPollDlq() {
        return contestPollDlq;
    }

    public void setContestPollDlq(String contestPollDlq) {
        this.contestPollDlq = contestPollDlq;
    }

    public String getContestPollDlRoutingKey() {
        return contestPollDlRoutingKey;
    }

    public void setContestPollDlRoutingKey(String contestPollDlRoutingKey) {
        this.contestPollDlRoutingKey = contestPollDlRoutingKey;
    }

    public String getContestPollDelayQueue() {
        return contestPollDelayQueue;
    }

    public void setContestPollDelayQueue(String contestPollDelayQueue) {
        this.contestPollDelayQueue = contestPollDelayQueue;
    }

    public String getContestPollDelayRoutingKey() {
        return contestPollDelayRoutingKey;
    }

    public void setContestPollDelayRoutingKey(String contestPollDelayRoutingKey) {
        this.contestPollDelayRoutingKey = contestPollDelayRoutingKey;
    }

    public long getInitialPollDelayMs() {
        return initialPollDelayMs;
    }

    public void setInitialPollDelayMs(long initialPollDelayMs) {
        this.initialPollDelayMs = initialPollDelayMs;
    }

    public long getPollDelayMs() {
        return pollDelayMs;
    }

    public void setPollDelayMs(long pollDelayMs) {
        this.pollDelayMs = pollDelayMs;
    }

    public long getMaxPollDelayMs() {
        return maxPollDelayMs;
    }

    public void setMaxPollDelayMs(long maxPollDelayMs) {
        this.maxPollDelayMs = maxPollDelayMs;
    }

    public int getMaxPollAttempts() {
        return maxPollAttempts;
    }

    public void setMaxPollAttempts(int maxPollAttempts) {
        this.maxPollAttempts = maxPollAttempts;
    }

    public long getMessageDedupTtlMs() {
        return messageDedupTtlMs;
    }

    public void setMessageDedupTtlMs(long messageDedupTtlMs) {
        this.messageDedupTtlMs = messageDedupTtlMs;
    }

    public long getCompensationFixedDelayMs() {
        return compensationFixedDelayMs;
    }

    public void setCompensationFixedDelayMs(long compensationFixedDelayMs) {
        this.compensationFixedDelayMs = compensationFixedDelayMs;
    }

    public long getSubmitCompensationDelaySeconds() {
        return submitCompensationDelaySeconds;
    }

    public void setSubmitCompensationDelaySeconds(long submitCompensationDelaySeconds) {
        this.submitCompensationDelaySeconds = submitCompensationDelaySeconds;
    }

    public long getPollCompensationDelaySeconds() {
        return pollCompensationDelaySeconds;
    }

    public void setPollCompensationDelaySeconds(long pollCompensationDelaySeconds) {
        this.pollCompensationDelaySeconds = pollCompensationDelaySeconds;
    }

    public long getCompensationThrottleSeconds() {
        return compensationThrottleSeconds;
    }

    public void setCompensationThrottleSeconds(long compensationThrottleSeconds) {
        this.compensationThrottleSeconds = compensationThrottleSeconds;
    }

    public int getCompensationBatchSize() {
        return compensationBatchSize;
    }

    public void setCompensationBatchSize(int compensationBatchSize) {
        this.compensationBatchSize = compensationBatchSize;
    }
}
