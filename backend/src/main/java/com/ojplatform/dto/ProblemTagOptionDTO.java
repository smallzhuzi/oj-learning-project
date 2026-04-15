package com.ojplatform.dto;

/**
 * 题目标签选项数据传输对象。
 */
public class ProblemTagOptionDTO {

    /**
     * 键。
     */
    private String key;
    /**
     * 标签名称。
     */
    private String label;
    /**
     * 类型。
     */
    private String type;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
