package com.ojplatform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ojplatform.dto.LoginDTO;
import com.ojplatform.dto.LoginResponse;
import com.ojplatform.dto.RegisterDTO;
import com.ojplatform.dto.UpdateProfileDTO;
import com.ojplatform.dto.ChangePasswordDTO;
import com.ojplatform.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /** 用户注册 */
    LoginResponse register(RegisterDTO dto);

    /** 用户登录 */
    LoginResponse login(LoginDTO dto);

    /** 获取当前用户信息（脱敏） */
    User getCurrentUser(Long userId);

    /** 通过邮箱重置密码 */
    void resetPassword(String email, String newPassword);

    /** 修改用户资料 */
    void updateProfile(Long userId, UpdateProfileDTO dto);

    /** 修改密码（需旧密码验证） */
    void changePassword(Long userId, ChangePasswordDTO dto);
}
