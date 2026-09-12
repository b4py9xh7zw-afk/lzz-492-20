package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.dto.injury.*;
import com.scaffolding.entity.InjuryInsurance;
import com.scaffolding.entity.InjuryReport;

/**
 * 工伤上报服务接口
 *
 * @author scaffolding
 */
public interface InjuryReportService extends IService<InjuryReport> {

    /**
     * 分页查询（按当前登录角色自动做数据隔离）
     */
    Page<InjuryReport> pageQuery(Long current, Long size, String reportStatus,
                                 String injuryType, String keyword);

    /**
     * 主管手机端新建/暂存/提交工伤上报（写入时间地点见证人送医岗位班次，回写排班档案留痕）
     */
    InjuryReport saveReport(InjuryReportSaveRequest request);

    /**
     * 详情（含材料清单缺件提示、保险资料、排班档案事件）
     */
    InjuryReportDetail detail(Long id);

    /**
     * 材料清单核对（上传/删除材料后实时返回）
     */
    MaterialChecklist checklist(Long reportId);

    /**
     * 劳务公司补充/更新保险资料
     */
    InjuryInsurance upsertInsurance(Long reportId, InjuryInsurance insurance);

    /**
     * 提交结论（stop-停工 / resume-复工），结论回写排班档案并留痕
     */
    InjuryReport conclude(Long reportId, ConclusionRequest request);
}
