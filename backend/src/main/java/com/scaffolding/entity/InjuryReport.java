package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 工伤上报单实体类
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("injury_report")
public class InjuryReport extends BaseEntity {

    /** 上报单号 */
    private String reportNo;

    /** 项目ID */
    private Long projectId;

    /** 关联排班档案ID */
    private Long scheduleId;

    /** 受伤工人ID */
    private Long workerId;

    /** 受伤工人姓名（冗余） */
    private String workerName;

    /** 岗位（来自排班档案） */
    private String postName;

    /** 班次（day/night/middle） */
    private String shiftName;

    /** 受伤时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime injuryTime;

    /** 受伤地点 */
    private String injuryLocation;

    /** 受伤经过与伤情描述 */
    private String injuryDesc;

    /** 伤情类型（outpatient-门诊/轻伤，hospitalized-住院，disability-疑似伤残，death-工亡） */
    private String injuryType;

    /** 送医医院 */
    private String hospital;

    /** 送医时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime hospitalTime;

    /** 医院诊断 */
    private String hospitalDiagnosis;

    /** 上报主管用户ID */
    private Long reporterId;

    /** 上报主管姓名 */
    private String reporterName;

    /** 上报时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime reportTime;

    /** 单据状态（draft-待提交，reported-已上报，submitted-材料已提交，concluded-已结论） */
    private String reportStatus;

    /** 复工/停工结论（stop-停工，resume-复工） */
    private String conclusion;

    /** 结论说明 */
    private String conclusionRemark;

    /** 停工开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate stopStartDate;

    /** 预计复工日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate expectedResumeDate;

    /** 实际复工日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate actualResumeDate;

    /** 结论确认人 */
    private String concludedBy;

    /** 结论确认时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime concludedTime;

    /** 材料完成度（0-100） */
    private Integer materialProgress;
}
