package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 比赛提交表实体类
 * 对应数据库表：contest_submissions
 * 独立于普通提交的比赛专用提交记录
 */
@TableName("contest_submissions")
public class ContestSubmission implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 比赛提交记录唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 比赛 ID */
    private Long contestId;

    /** 提交者用户 ID */
    private Long userId;

    /** 队伍 ID（组队赛时使用） */
    private Long teamId;

    /** 题目 ID */
    private Long problemId;

    /** 编程语言 */
    private String language;

    /** 用户提交的源代码 */
    private String code;

    /** 判题状态 */
    private String status;

    /** 运行耗时 */
    private String runtime;

    /** 内存消耗 */
    private String memory;

    /** 通过的测试用例数 */
    private Integer totalCorrect;

    /** 总测试用例数 */
    private Integer totalTestcases;

    /** OI 赛制的该次提交得分 */
    private Integer score;

    /** 远程 OJ 提交 ID */
    private String remoteSubmissionId;

    /** 提交时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submittedAt;

    // ==================== Getter / Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public Integer getTotalCorrect() {
        return totalCorrect;
    }

    public void setTotalCorrect(Integer totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    public Integer getTotalTestcases() {
        return totalTestcases;
    }

    public void setTotalTestcases(Integer totalTestcases) {
        this.totalTestcases = totalTestcases;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getRemoteSubmissionId() {
        return remoteSubmissionId;
    }

    public void setRemoteSubmissionId(String remoteSubmissionId) {
        this.remoteSubmissionId = remoteSubmissionId;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
