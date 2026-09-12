package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 工伤保险资料实体类（劳务公司补充）
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("injury_insurance")
public class InjuryInsurance extends BaseEntity {

    /** 工伤上报单ID */
    private Long reportId;

    /** 参保人姓名 */
    private String insuredName;

    /** 承保保险公司/社保经办机构 */
    private String insuranceCompany;

    /** 保单号/社保电脑号 */
    private String policyNo;

    /** 起保日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate coverageStartDate;

    /** 终止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate coverageEndDate;

    /** 理赔状态（not_filed-未报案，reported-已报案，claiming-理赔中，paid-已赔付，rejected-拒赔） */
    private String claimStatus;

    /** 报案号/理赔受理号 */
    private String claimNo;

    /** 理赔金额（元） */
    private BigDecimal claimAmount;

    /** 劳务经办人 */
    private String contactName;

    /** 经办人电话 */
    private String contactPhone;

    /** 备注 */
    private String remark;
}
