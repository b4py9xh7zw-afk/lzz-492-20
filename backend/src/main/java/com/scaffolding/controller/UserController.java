package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.User;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 
 * @author scaffolding
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<User> login(@RequestBody LoginRequest request) {
        try {
            if (!StringUtils.hasText(request.getUsername())) {
                return Result.error("用户名不能为空");
            }
            if (!StringUtils.hasText(request.getPassword())) {
                return Result.error("密码不能为空");
            }
            
            User user = userService.login(request.getUsername(), request.getPassword());
            // 不返回密码
            user.setPassword(null);
            return Result.success("登录成功", user);
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("登录失败", e);
            return Result.error("登录失败：" + e.getMessage());
        }
    }

    @GetMapping("/page")
    @ApiOperation("分页查询用户")
    public Result<PageResult<User>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname) {
        Page<User> page = userService.pageQuery(current, size, username, nickname);
        
        // 不返回密码
        page.getRecords().forEach(user -> user.setPassword(null));
        
        PageResult<User> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }

    @PostMapping
    @ApiOperation("新增用户")
    public Result<?> save(@RequestBody User user) {
        try {
            if (!StringUtils.hasText(user.getUsername())) {
                return Result.error("用户名不能为空");
            }
            if (!StringUtils.hasText(user.getPassword())) {
                return Result.error("密码不能为空");
            }
            
            // 检查用户名是否已存在
            if (userService.checkUsernameExists(user.getUsername(), null)) {
                return Result.error("用户名已存在，不能重复");
            }
            
            userService.save(user);
            return Result.success("新增成功");
        } catch (Exception e) {
            log.error("新增用户失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新用户")
    public Result<?> update(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            
            if (StringUtils.hasText(user.getUsername())) {
                // 检查用户名是否已存在（排除当前用户）
                if (userService.checkUsernameExists(user.getUsername(), id)) {
                    return Result.error("用户名已存在，不能重复");
                }
            }
            
            userService.updateById(user);
            return Result.success("更新成功");
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除用户")
    public Result<?> delete(@PathVariable Long id) {
        try {
            userService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}/reset-password")
    @ApiOperation("重置密码")
    public Result<?> resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        try {
            if (!StringUtils.hasText(request.getNewPassword())) {
                return Result.error("新密码不能为空");
            }
            
            userService.resetPassword(id, request.getNewPassword());
            return Result.success("重置密码成功");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return Result.error("重置密码失败：" + e.getMessage());
        }
    }

    /**
     * 登录请求对象
     */
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * 重置密码请求对象
     */
    public static class ResetPasswordRequest {
        private String newPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
