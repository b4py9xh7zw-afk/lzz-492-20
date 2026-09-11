package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.User;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.UserMapper;
import com.scaffolding.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现类
 * 
 * @author scaffolding
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = this.getOne(wrapper);
        
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }
        
        if (!password.equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        
        return user;
    }

    @Override
    public Page<User> pageQuery(Long current, Long size, String username, String nickname) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        if (StringUtils.hasText(nickname)) {
            wrapper.like(User::getNickname, nickname);
        }
        
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public boolean checkUsernameExists(String username, Long excludeId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(User::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }

    @Override
    public boolean resetPassword(Long id, String newPassword) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(newPassword);
        return this.updateById(user);
    }
}
