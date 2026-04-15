package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonRawValue;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 平台标签实体类。
 */
@TableName("platform_tags")
public class PlatformTag implements Serializable {

    /**
     * 唯一标识。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 在线判题平台。
     */
    private String ojPlatform;

    /**
     * 来源标签 ID。
     */
    private String sourceTagId;

    /**
     * 来源标识。
     */
    private String sourceSlug;

    /**
     * 来源Name。
     */
    private String sourceName;

    /**
     * 规范化键。
     */
    private String normalizedKey;

    /**
     * 标签类型 ID。
     */
    private Long tagTypeId;

    /**
     * 标签 ID。
     */
    private Long tagId;

    /**
     * 元数据。
     */
    @JsonRawValue
    private String metadata;

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOjPlatform() {
        return ojPlatform;
    }

    public void setOjPlatform(String ojPlatform) {
        this.ojPlatform = ojPlatform;
    }

    public String getSourceTagId() {
        return sourceTagId;
    }

    public void setSourceTagId(String sourceTagId) {
        this.sourceTagId = sourceTagId;
    }

    public String getSourceSlug() {
        return sourceSlug;
    }

    public void setSourceSlug(String sourceSlug) {
        this.sourceSlug = sourceSlug;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getNormalizedKey() {
        return normalizedKey;
    }

    public void setNormalizedKey(String normalizedKey) {
        this.normalizedKey = normalizedKey;
    }

    public Long getTagTypeId() {
        return tagTypeId;
    }

    public void setTagTypeId(Long tagTypeId) {
        this.tagTypeId = tagTypeId;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    @JsonRawValue
    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
