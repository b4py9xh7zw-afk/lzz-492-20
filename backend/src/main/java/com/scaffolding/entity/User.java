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

    /**
     * 角色（admin-平台管理员，supervisor-现场主管，labor-劳务公司，enterprise-用工企业）
     */
    private String role;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 绑定项目ID（主管/企业按项目做数据隔离）
     */
    private Long projectId;

    /**
     * 所属公司名称（劳务公司账号，按合作关系过滤数据）
     */
    private String companyName;
}
