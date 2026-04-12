package com.ojplatform.dto;

/**
 * LeetCode 判题结果 DTO
 * 对应轮询接口 /submissions/detail/{id}/check/ 的返回结果
 */
public class LeetCodeJudgeResult {

    /** 判题是否完成（state 为 SUCCESS 时为 true） */
    private boolean finished;

    /** 判题状态（SUCCESS / PENDING / STARTED） */
    private String state;

    /** 结果描述（Accepted / Wrong Answer / Time Limit Exceeded 等） */
    private String statusMsg;

    /** 运行耗时（如 "4 ms"） */
    private String runtime;

    /** 内存消耗（如 "39.2 MB"） */
    private String memory;

    /** 通过的测试用例数 */
    private Integer totalCorrect;

    /** 总测试用例数 */
    private Integer totalTestcases;

    /** 是否运行成功 */
    private Boolean runSuccess;

    // ==================== Getter / Setter ====================

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
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

    public Boolean getRunSuccess() {
        return runSuccess;
    }

    public void setRunSuccess(Boolean runSuccess) {
        this.runSuccess = runSuccess;
    }
}
