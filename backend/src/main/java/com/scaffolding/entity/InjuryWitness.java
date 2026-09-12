package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工伤见证人实体类
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("injury_witness")
public class InjuryWitness extends BaseEntity {

    /** 工伤上报单ID */
    private Long reportId;

    /** 见证人姓名 */
    private String witnessName;

    /** 见证人联系电话 */
    private String witnessPhone;

    /** 身份（coworker-同班组工人，manager-现场管理人员，other-其他） */
    private String witnessType;

    /** 见证情况说明 */
    private String statement;
}
