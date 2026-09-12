package com.scaffolding.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户上下文（由请求头 X-User-Id 解析）
 *
 * @author scaffolding
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    /** 用户ID */
    private Long userId;

    /** 昵称/姓名 */
    private String nickname;

    /** 角色：admin/supervisor/labor/enterprise */
    private String role;

    /** 绑定项目ID（主管、企业） */
    private Long projectId;

    /** 所属公司（劳务） */
    private String companyName;
}
