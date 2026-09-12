package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工人实体类
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("worker")
public class Worker extends BaseEntity {

    /** 姓名 */
    private String workerName;

    /** 手机号 */
    private String phone;

    /** 身份证号 */
    private String idCard;

    /** 所属劳务公司 */
    private String laborCompany;

    /** 状态（active-在职，disabled-停用） */
    private String status;
}
