package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.dto.injury.*;
import com.scaffolding.entity.InjuryInsurance;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.entity.InjuryReport;
import com.scaffolding.service.InjuryReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 工伤上报控制器
 * <p>
 * 主管手机端记录时间/地点/见证人/送医医院/岗位班次；平台返回缺件提示；
 * 劳务公司补充保险资料；企业仅查看本项目；复工/停工结论回写排班档案。
 *
 * @author scaffolding
 */
@Slf4j
@RestController
@RequestMapping("/injury")
@Api(tags = "工伤上报材料包")
public class InjuryController {

    @Autowired
    private InjuryReportService injuryReportService;

    @PostMapping("/report")
    @ApiOperation("主管手机端新建/暂存/提交工伤上报")
    public Result<InjuryReport> saveReport(@RequestBody InjuryReportSaveRequest request) {
        try {
            return Result.success(Boolean.TRUE.equals(request.getSubmit()) ? "提交成功" : "已保存草稿",
                    injuryReportService.saveReport(request));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("保存工伤上报失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/page")
    @ApiOperation("分页查询工伤上报单（自动按角色隔离）")
    public Result<PageResult<InjuryReport>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String reportStatus,
            @RequestParam(required = false) String injuryType,
            @RequestParam(required = false) String keyword) {
        Page<InjuryReport> page = injuryReportService.pageQuery(current, size, reportStatus, injuryType, keyword);
        PageResult<InjuryReport> pageResult = new PageResult<>(
                page.getTotal(), page.getRecords(), page.getCurrent(), page.getSize());
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    @ApiOperation("工伤上报详情（含材料缺件清单、保险资料、排班档案事件）")
    public Result<InjuryReportDetail> detail(@PathVariable Long id) {
        try {
            return Result.success(injuryReportService.detail(id));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询工伤详情失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/checklist")
    @ApiOperation("材料缺件清单（平台提示还缺哪些证明）")
    public Result<MaterialChecklist> checklist(@PathVariable Long id) {
        try {
            return Result.success(injuryReportService.checklist(id));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("核对材料清单失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/insurance")
    @ApiOperation("劳务公司补充保险资料")
    public Result<InjuryInsurance> saveInsurance(@PathVariable Long id,
                                                 @RequestBody InjuryInsurance insurance) {
        try {
            return Result.success("保险资料已保存", injuryReportService.upsertInsurance(id, insurance));
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("保存保险资料失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/conclusion")
    @ApiOperation("复工/停工结论（回写排班档案并留痕）")
    public Result<InjuryReport> conclusion(@PathVariable Long id,
                                           @RequestBody ConclusionRequest request) {
        try {
            InjuryReport report = injuryReportService.conclude(id, request);
            String msg = "resume".equals(request.getConclusion()) ? "复工结论已回写排班档案" : "停工结论已回写排班档案";
            return Result.success(msg, report);
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("提交工伤结论失败", e);
            return Result.error(e.getMessage());
        }
    }
}
