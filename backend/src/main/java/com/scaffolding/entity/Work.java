package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 工作管理实体类
 * 
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("work")
public class Work extends BaseEntity {

    /**
     * 工作名称
     */
    private String workName;

    /**
     * 工作内容
     */
    private String workContent;

    /**
     * 工作状态（pending-待处理，in_progress-进行中，completed-已完成，cancelled-已取消）
     */
    private String workStatus;

    /**
     * 工作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime workTime;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")

    private LocalDateTime endTime;

    /**
     * 优先级（low-低，normal-普通，high-高，urgent-紧急）
     */
    private String priority;

    /**
     * 备注
     */
    private String remark;
}
