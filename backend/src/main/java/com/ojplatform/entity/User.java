package com.ojplatform.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户表实体类
 * 对应数据库表：users
 */
@TableName("users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户唯一标识 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（登录名，不可重复） */
    private String username;

    /** 用户邮箱（不可重复，用于找回密码等） */
    private String email;

    /** 加密后的密码（BCrypt） */
    private String password;

    /** 用户角色（user / admin） */
    private String role;

    /** 账号状态（active / disabled） */
    private String status;

    /** 注册时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ==================== Getter / Setter ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
