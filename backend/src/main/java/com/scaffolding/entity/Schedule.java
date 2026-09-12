package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班档案实体类（工伤结论回写对象）
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("schedule")
public class Schedule extends BaseEntity {

    /** 项目ID */
    private Long projectId;

    /** 工人ID */
    private Long workerId;

    /** 岗位 */
    private String postName;

    /** 班次（day-白班，night-夜班，middle-中班） */
    private String shift;

    /** 排班日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate scheduleDate;

    /** 上班时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /** 下班时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /** 档案状态（normal-正常排班，injury_stop-工伤停工，resumed-已复工） */
    private String scheduleStatus;

    /** 关联工伤上报单ID */
    private Long injuryReportId;

    /** 停工开始日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate stopStartDate;

    /** 复工日期 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate resumeDate;

    /** 最新结论（stop-停工，resume-复工） */
    private String conclusion;

    /** 备注 */
    private String remark;
}
