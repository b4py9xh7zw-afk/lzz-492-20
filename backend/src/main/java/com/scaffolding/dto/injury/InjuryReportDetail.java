package com.scaffolding.dto.injury;

import com.scaffolding.entity.*;
import lombok.Data;

import java.util.List;

/**
 * 工伤上报单详情（主单 + 见证人 + 材料 + 保险 + 清单核对 + 排班档案事件）
 *
 * @author scaffolding
 */
@Data
public class InjuryReportDetail {

    /** 上报单 */
    private InjuryReport report;

    /** 项目信息 */
    private Project project;

    /** 工人信息 */
    private Worker worker;

    /** 关联排班档案 */
    private Schedule schedule;

    /** 见证人 */
    private List<InjuryWitness> witnesses;

    /** 已上传材料 */
    private List<InjuryMaterial> materials;

    /** 保险资料（劳务补充） */
    private InjuryInsurance insurance;

    /** 材料清单核对结果 */
    private MaterialChecklist checklist;

    /** 排班档案事件留痕 */
    private List<ScheduleEvent> scheduleEvents;

    /** 当前登录人是否可编辑主单（主管） */
    private Boolean canEditReport;

    /** 当前登录人是否可补充保险资料（劳务） */
    private Boolean canEditInsurance;

    /** 当前登录人是否可下复工/停工结论 */
    private Boolean canConclude;
}
