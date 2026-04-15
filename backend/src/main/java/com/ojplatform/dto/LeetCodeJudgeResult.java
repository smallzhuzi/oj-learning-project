package com.ojplatform.dto;

/**
 * 力扣判题结果数据传输对象。
 */
public class LeetCodeJudgeResult {

    /**
     * 是否完成。
     */
    private boolean finished;

    /**
     * 状态值。
     */
    private String state;

    /**
     * 状态Msg。
     */
    private String statusMsg;

    /**
     * 运行耗时。
     */
    private String runtime;

    /**
     * 内存消耗。
     */
    private String memory;

    /**
     * 通过用例数。
     */
    private Integer totalCorrect;

    /**
     * 总用例数。
     */
    private Integer totalTestcases;

    /**
     * 是否运行成功。
     */
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
