package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 * 
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    /**
     * 用户名（账号）
     */
    private String username;

    /**
     * 密码（不加密）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;
}
