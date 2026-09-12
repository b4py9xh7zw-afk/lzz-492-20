package com.scaffolding.dto.injury;

import com.scaffolding.entity.InjuryWitness;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 主管手机端工伤上报保存请求
 *
 * @author scaffolding
 */
@Data
public class InjuryReportSaveRequest {

    /** 上报单ID（不传=新建） */
    private Long id;

    /** 排班档案ID（岗位班次来源） */
    private Long scheduleId;

    /** 项目ID（新建时可由排班带出，也可直接指定） */
    private Long projectId;

    /** 工人ID */
    private Long workerId;

    /** 受伤时间 */
    private LocalDateTime injuryTime;

    /** 受伤地点 */
    private String injuryLocation;

    /** 受伤经过 */
    private String injuryDesc;

    /** 伤情类型 */
    private String injuryType;

    /** 送医医院 */
    private String hospital;

    /** 送医时间 */
    private LocalDateTime hospitalTime;

    /** 是否直接提交（false=暂存草稿） */
    private Boolean submit;

    /** 见证人列表 */
    private List<InjuryWitness> witnesses;
}
