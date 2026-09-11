package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.User;

/**
 * 用户服务接口
 * 
 * @author scaffolding
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     */
    User login(String username, String password);

    /**
     * 分页查询用户
     */
    Page<User> pageQuery(Long current, Long size, String username, String nickname);

    /**
     * 检查用户名是否存在
     */
    boolean checkUsernameExists(String username, Long excludeId);

    /**
     * 重置密码
     */
    boolean resetPassword(Long id, String newPassword);
}
