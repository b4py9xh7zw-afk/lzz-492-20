package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 项目实体类
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("project")
public class Project extends BaseEntity {

    /** 项目名称 */
    private String projectName;

    /** 用工企业名称 */
    private String enterpriseName;

    /** 合作劳务公司名称 */
    private String laborCompany;

    /** 项目地址 */
    private String address;

    /** 状态（active-进行中，closed-已结束） */
    private String status;
}
