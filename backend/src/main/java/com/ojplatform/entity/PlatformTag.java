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
 * 平台标签映射实体类
 * 对应表：platform_tags
 */
@TableName("platform_tags")
public class PlatformTag implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ojPlatform;

    private String sourceTagId;

    private String sourceSlug;

    private String sourceName;

    private String normalizedKey;

    private Long tagTypeId;

    private Long tagId;

    @JsonRawValue
    private String metadata;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

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
