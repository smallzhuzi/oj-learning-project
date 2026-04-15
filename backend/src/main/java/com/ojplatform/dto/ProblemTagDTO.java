package com.ojplatform.dto;

/**
 * 题目标签数据传输对象。
 */
public class ProblemTagDTO {

    /**
     * 唯一标识。
     */
    private Long id;

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

    /**
     * 来源Name。
     */
    private String sourceName;

    /**
     * 来源标识。
     */
    private String sourceSlug;

    /**
     * 在线判题平台。
     */
    private String ojPlatform;

    // ===== 兼容旧前端字段 =====
    public String getName() {
        return label;
    }

    public String getSlug() {
        return key;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getSourceSlug() {
        return sourceSlug;
    }

    public void setSourceSlug(String sourceSlug) {
        this.sourceSlug = sourceSlug;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }
}
