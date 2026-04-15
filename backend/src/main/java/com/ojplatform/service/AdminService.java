package com.ojplatform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ojplatform.dto.AdminCreateUserDTO;
import com.ojplatform.entity.User;

/**
 * 管理相关业务接口。
 */
public interface AdminService {

    /** 分页查询用户列表 */
    IPage<User> getUserList(int page, int size, String keyword);

    /** 切换用户状态（启用/禁用） */
    void toggleUserStatus(Long userId);

    /** 重置用户密码，返回重置后的密码明文 */
    String resetUserPassword(Long userId);

    /** 删除用户 */
    void deleteUser(Long userId);

    /** 创建用户 */
    void createUser(AdminCreateUserDTO dto);
}
