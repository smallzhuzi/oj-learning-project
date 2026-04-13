package com.ojplatform.dto;

/**
 * 统一题目标签 DTO
 * 兼容旧前端的 name/slug 字段，同时提供新的 key/label/type 字段。
 */
public class ProblemTagDTO {

    private Long id;

    /** 统一标签 key，兼容旧字段语义 */
    private String key;

    /** 标签显示名 */
    private String label;

    /** 标签类型，如 algorithm / data_structure / source */
    private String type;

    /** 平台原始标签名称 */
    private String sourceName;

    /** 平台原始标签 slug */
    private String sourceSlug;

    /** 平台标识 */
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
