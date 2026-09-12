package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 排班档案事件实体类（工伤上报/停工/复工留痕）
 *
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("schedule_event")
public class ScheduleEvent extends BaseEntity {

    /** 排班档案ID */
    private Long scheduleId;

    /** 关联工伤上报单ID */
    private Long reportId;

    /** 事件类型（injury_report-工伤上报，injury_stop-停工结论，injury_resume-复工结论） */
    private String eventType;

    /** 事件内容 */
    private String eventContent;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** 事件时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime eventTime;
}
