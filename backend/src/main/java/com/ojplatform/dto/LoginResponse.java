package com.ojplatform.dto;

/**
 * 登录响应数据传输对象。
 */
public class LoginResponse {

    /**
     * 登录令牌。
     */
    private String token;
    /**
     * 用户ID。
     */
    private Long userId;
    /**
     * 用户名。
     */
    private String username;
    /**
     * 邮箱。
     */
    private String email;
    /**
     * 角色。
     */
    private String role;

    public LoginResponse(String token, Long userId, String username, String email, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // ==================== Getter / Setter ====================

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
