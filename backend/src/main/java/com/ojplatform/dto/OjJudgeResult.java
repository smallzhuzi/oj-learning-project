package com.ojplatform.dto;

/**
 * 平台无关的判题结果 DTO
 * 统一不同 OJ 平台的评测结果格式
 */
public class OjJudgeResult {

    /** 判题是否完成 */
    private boolean finished;

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

    // ==================== Getter / Setter ====================

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
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
}
