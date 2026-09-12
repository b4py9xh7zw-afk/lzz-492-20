package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.dto.injury.*;
import com.scaffolding.entity.*;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.*;
import com.scaffolding.security.CurrentUserHolder;
import com.scaffolding.security.LoginUser;
import com.scaffolding.service.InjuryReportService;
import com.scaffolding.service.MaterialCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工伤上报服务实现
 * <p>
 * 数据隔离：企业只能看本项目；劳务只能看本公司合作项目/工人；主管只能看本项目；管理员全部。
 *
 * @author scaffolding
 */
@Service
public class InjuryReportServiceImpl extends ServiceImpl<InjuryReportMapper, InjuryReport>
        implements InjuryReportService {

    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private ScheduleEventMapper scheduleEventMapper;
    @Autowired
    private WorkerMapper workerMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private InjuryWitnessMapper witnessMapper;
    @Autowired
    private InjuryMaterialMapper materialMapper;
    @Autowired
    private InjuryInsuranceMapper insuranceMapper;
    @Autowired
    private MaterialCatalog materialCatalog;

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ============================ 查询 ============================

    @Override
    public Page<InjuryReport> pageQuery(Long current, Long size, String reportStatus,
                                        String injuryType, String keyword) {
        LoginUser user = CurrentUserHolder.require();
        Page<InjuryReport> page = new Page<>(current, size);
        LambdaQueryWrapper<InjuryReport> wrapper = new LambdaQueryWrapper<>();

        // 角色数据隔离
        if ("enterprise".equals(user.getRole()) || "supervisor".equals(user.getRole())) {
            wrapper.eq(InjuryReport::getProjectId, user.getProjectId());
        } else if ("labor".equals(user.getRole())) {
            List<Long> projectIds = projectIdsOfLabor(user);
            if (projectIds.isEmpty()) {
                // 没有合作项目直接返回空页
                return page;
            }
            wrapper.in(InjuryReport::getProjectId, projectIds);
        }

        if (StringUtils.hasText(reportStatus)) {
            wrapper.eq(InjuryReport::getReportStatus, reportStatus);
        }
        if (StringUtils.hasText(injuryType)) {
            wrapper.eq(InjuryReport::getInjuryType, injuryType);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(InjuryReport::getReportNo, keyword)
                    .or().like(InjuryReport::getWorkerName, keyword)
                    .or().like(InjuryReport::getHospital, keyword)
                    .or().like(InjuryReport::getInjuryLocation, keyword));
        }
        wrapper.orderByDesc(InjuryReport::getInjuryTime);
        return this.page(page, wrapper);
    }

    @Override
    public InjuryReportDetail detail(Long id) {
        LoginUser user = CurrentUserHolder.require();
        InjuryReport report = this.getById(id);
        if (report == null) {
            throw new BusinessException("工伤上报单不存在");
        }
        ensureProjectVisible(user, report.getProjectId());

        InjuryReportDetail detail = new InjuryReportDetail();
        detail.setReport(report);
        detail.setProject(projectMapper.selectById(report.getProjectId()));
        detail.setWorker(workerMapper.selectById(report.getWorkerId()));
        if (report.getScheduleId() != null) {
            detail.setSchedule(scheduleMapper.selectById(report.getScheduleId()));
        }

        detail.setWitnesses(witnessMapper.selectList(new LambdaQueryWrapper<InjuryWitness>()
                .eq(InjuryWitness::getReportId, id)
                .orderByAsc(InjuryWitness::getId)));

        List<InjuryMaterial> materials = materialMapper.selectList(new LambdaQueryWrapper<InjuryMaterial>()
                .eq(InjuryMaterial::getReportId, id)
                .orderByDesc(InjuryMaterial::getCreateTime));
        detail.setMaterials(materials);

        detail.setInsurance(insuranceMapper.selectOne(new LambdaQueryWrapper<InjuryInsurance>()
                .eq(InjuryInsurance::getReportId, id)));

        MaterialChecklist checklist = materialCatalog.evaluate(report, materials);
        detail.setChecklist(checklist);

        List<ScheduleEvent> events = new ArrayList<>();
        if (report.getScheduleId() != null) {
            events = scheduleEventMapper.selectList(new LambdaQueryWrapper<ScheduleEvent>()
                    .eq(ScheduleEvent::getScheduleId, report.getScheduleId())
                    .orderByDesc(ScheduleEvent::getEventTime)
                    .orderByDesc(ScheduleEvent::getId));
        }
        detail.setScheduleEvents(events);

        detail.setCanEditReport("supervisor".equals(user.getRole()) || "admin".equals(user.getRole()));
        detail.setCanEditInsurance("labor".equals(user.getRole()) || "admin".equals(user.getRole()));
        detail.setCanConclude("supervisor".equals(user.getRole()) || "admin".equals(user.getRole()));
        return detail;
    }

    @Override
    public MaterialChecklist checklist(Long reportId) {
        LoginUser user = CurrentUserHolder.require();
        InjuryReport report = this.getById(reportId);
        if (report == null) {
            throw new BusinessException("工伤上报单不存在");
        }
        ensureProjectVisible(user, report.getProjectId());
        List<InjuryMaterial> materials = materialMapper.selectList(
                new LambdaQueryWrapper<InjuryMaterial>().eq(InjuryMaterial::getReportId, reportId));
        return materialCatalog.evaluate(report, materials);
    }

    // ============================ 主管上报 ============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InjuryReport saveReport(InjuryReportSaveRequest request) {
        LoginUser user = CurrentUserHolder.require();
        if (!"supervisor".equals(user.getRole()) && !"admin".equals(user.getRole())) {
            throw new BusinessException(403, "只有现场主管可以上报工伤");
        }

        InjuryReport report;
        boolean firstReport = false;
        if (request.getId() != null) {
            report = this.getById(request.getId());
            if (report == null) {
                throw new BusinessException("工伤上报单不存在");
            }
            ensureProjectVisible(user, report.getProjectId());
        } else {
            report = new InjuryReport();
            report.setReportNo(generateReportNo());
            report.setReportStatus("draft");
            report.setMaterialProgress(0);
            firstReport = true;
        }

        // 排班档案带出：项目、工人、岗位、班次
        Schedule schedule = null;
        if (request.getScheduleId() != null) {
            schedule = scheduleMapper.selectById(request.getScheduleId());
            if (schedule == null) {
                throw new BusinessException("所选排班档案不存在");
            }
            ensureProjectVisible(user, schedule.getProjectId());
            report.setScheduleId(schedule.getId());
            report.setProjectId(schedule.getProjectId());
            report.setWorkerId(schedule.getWorkerId());
            report.setPostName(schedule.getPostName());
            report.setShiftName(schedule.getShift());
        } else if (request.getProjectId() != null && request.getWorkerId() != null) {
            ensureProjectVisible(user, request.getProjectId());
            report.setProjectId(request.getProjectId());
            report.setWorkerId(request.getWorkerId());
        }

        if (report.getProjectId() == null || report.getWorkerId() == null) {
            throw new BusinessException("请选择项目、工人（或直接选择排班档案）");
        }

        Worker worker = workerMapper.selectById(report.getWorkerId());
        if (worker != null) {
            report.setWorkerName(worker.getWorkerName());
        }

        if (StringUtils.hasText(request.getInjuryLocation())) {
            report.setInjuryLocation(request.getInjuryLocation());
        }
        if (request.getInjuryTime() != null) {
            report.setInjuryTime(request.getInjuryTime());
        }
        if (StringUtils.hasText(request.getInjuryDesc())) {
            report.setInjuryDesc(request.getInjuryDesc());
        }
        if (StringUtils.hasText(request.getInjuryType())) {
            report.setInjuryType(request.getInjuryType());
        }
        if (StringUtils.hasText(request.getHospital())) {
            report.setHospital(request.getHospital());
        }
        if (request.getHospitalTime() != null) {
            report.setHospitalTime(request.getHospitalTime());
        }

        if (report.getInjuryTime() == null) {
            throw new BusinessException("受伤时间不能为空");
        }
        if (!StringUtils.hasText(report.getInjuryLocation())) {
            throw new BusinessException("受伤地点不能为空");
        }

        boolean submit = Boolean.TRUE.equals(request.getSubmit());

        // 见证人（提交前校验，落库后统一全量覆盖）
        List<InjuryWitness> validWitnesses = new ArrayList<>();
        if (request.getWitnesses() != null) {
            validWitnesses = request.getWitnesses().stream()
                    .filter(w -> StringUtils.hasText(w.getWitnessName()))
                    .collect(Collectors.toList());
            if (submit && validWitnesses.isEmpty()) {
                throw new BusinessException("提交时至少填写1名见证人");
            }
        }

        // 提交校验：医院必填
        if (submit && !StringUtils.hasText(report.getHospital())) {
            throw new BusinessException("提交时请填写送医医院");
        }

        if (firstReport) {
            report.setReporterId(user.getUserId());
            report.setReporterName(user.getNickname());
            report.setReportTime(LocalDateTime.now());
            report.setCreateTime(LocalDateTime.now());
            report.setUpdateTime(LocalDateTime.now());
            this.save(report);
        } else {
            report.setUpdateTime(LocalDateTime.now());
            this.updateById(report);
        }

        // 见证人全量覆盖（新建时 report.getId() 已可用）
        if (request.getWitnesses() != null) {
            witnessMapper.delete(new LambdaQueryWrapper<InjuryWitness>()
                    .eq(InjuryWitness::getReportId, report.getId()));
            for (InjuryWitness w : validWitnesses) {
                w.setId(null);
                w.setReportId(report.getId());
                if (!StringUtils.hasText(w.getWitnessType())) {
                    w.setWitnessType("coworker");
                }
                w.setCreateTime(LocalDateTime.now());
                w.setUpdateTime(LocalDateTime.now());
                witnessMapper.insert(w);
            }
        }

        if (submit) {
            report.setReportStatus("reported");
            report.setReportTime(LocalDateTime.now());
            // 回写排班档案 + 留痕（草稿转提交或首次提交，只要档案尚未挂单即绑定）
            if (report.getScheduleId() != null) {
                Schedule linked = scheduleMapper.selectById(report.getScheduleId());
                if (linked != null && !report.getId().equals(linked.getInjuryReportId())) {
                    bindReportToSchedule(report);
                }
            }
            this.updateById(report);
        }

        return report;
    }

    /** 将上报单挂到排班档案并记录"工伤上报"事件 */
    private void bindReportToSchedule(InjuryReport report) {
        Schedule schedule = scheduleMapper.selectById(report.getScheduleId());
        if (schedule == null) {
            return;
        }
        schedule.setInjuryReportId(report.getId());
        if (!"injury_stop".equals(schedule.getScheduleStatus())
                && !"resumed".equals(schedule.getScheduleStatus())) {
            schedule.setScheduleStatus("injury_stop");
        }
        schedule.setUpdateTime(LocalDateTime.now());
        scheduleMapper.updateById(schedule);

        ScheduleEvent event = new ScheduleEvent();
        event.setScheduleId(schedule.getId());
        event.setReportId(report.getId());
        event.setEventType("injury_report");
        event.setEventContent(String.format("主管手机端上报工伤：%s，伤情类型：%s，送医：%s。",
                report.getInjuryLocation(), typeName(report.getInjuryType()),
                StringUtils.hasText(report.getHospital()) ? report.getHospital() : "待送医"));
        event.setOperatorId(report.getReporterId());
        event.setOperatorName(report.getReporterName());
        event.setEventTime(LocalDateTime.now());
        event.setCreateTime(LocalDateTime.now());
        event.setUpdateTime(LocalDateTime.now());
        scheduleEventMapper.insert(event);
    }

    // ============================ 劳务保险资料 ============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InjuryInsurance upsertInsurance(Long reportId, InjuryInsurance insurance) {
        LoginUser user = CurrentUserHolder.require();
        if (!"labor".equals(user.getRole()) && !"admin".equals(user.getRole())) {
            throw new BusinessException(403, "只有劳务公司可以补充保险资料");
        }
        InjuryReport report = this.getById(reportId);
        if (report == null) {
            throw new BusinessException("工伤上报单不存在");
        }
        ensureProjectVisible(user, report.getProjectId());

        InjuryInsurance exist = insuranceMapper.selectOne(new LambdaQueryWrapper<InjuryInsurance>()
                .eq(InjuryInsurance::getReportId, reportId));
        if (exist == null) {
            insurance.setId(null);
            insurance.setReportId(reportId);
            if (!StringUtils.hasText(insurance.getInsuredName())) {
                insurance.setInsuredName(report.getWorkerName());
            }
            if (!StringUtils.hasText(insurance.getClaimStatus())) {
                insurance.setClaimStatus("not_filed");
            }
            insurance.setCreateTime(LocalDateTime.now());
            insurance.setUpdateTime(LocalDateTime.now());
            insuranceMapper.insert(insurance);
            return insurance;
        }
        insurance.setId(exist.getId());
        insurance.setReportId(reportId);
        insurance.setUpdateTime(LocalDateTime.now());
        insuranceMapper.updateById(insurance);
        return insuranceMapper.selectById(exist.getId());
    }

    // ============================ 复工/停工结论 ============================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InjuryReport conclude(Long reportId, ConclusionRequest request) {
        LoginUser user = CurrentUserHolder.require();
        if (!"supervisor".equals(user.getRole()) && !"admin".equals(user.getRole())) {
            throw new BusinessException(403, "用工企业仅可查看，复工/停工结论由现场主管确认");
        }
        InjuryReport report = this.getById(reportId);
        if (report == null) {
            throw new BusinessException("工伤上报单不存在");
        }
        ensureProjectVisible(user, report.getProjectId());

        String conclusion = request.getConclusion();
        if (!"stop".equals(conclusion) && !"resume".equals(conclusion)) {
            throw new BusinessException("结论类型只能是 stop（停工）或 resume（复工）");
        }
        if ("stop".equals(conclusion) && request.getStopStartDate() == null) {
            throw new BusinessException("停工结论需填写停工开始日期");
        }
        if ("resume".equals(conclusion) && request.getActualResumeDate() == null) {
            throw new BusinessException("复工结论需填写实际复工日期");
        }

        report.setConclusion(conclusion);
        report.setConclusionRemark(request.getConclusionRemark());
        report.setStopStartDate("stop".equals(conclusion) ? request.getStopStartDate() : report.getStopStartDate());
        report.setExpectedResumeDate(request.getExpectedResumeDate());
        report.setActualResumeDate("resume".equals(conclusion) ? request.getActualResumeDate() : report.getActualResumeDate());
        report.setConcludedBy(user.getNickname());
        report.setConcludedTime(LocalDateTime.now());
        report.setReportStatus("concluded");
        report.setUpdateTime(LocalDateTime.now());
        this.updateById(report);

        // 结论回写排班档案并留痕
        if (report.getScheduleId() != null) {
            Schedule schedule = scheduleMapper.selectById(report.getScheduleId());
            if (schedule != null) {
                schedule.setInjuryReportId(report.getId());
                schedule.setConclusion(conclusion);
                ScheduleEvent event = new ScheduleEvent();
                event.setScheduleId(schedule.getId());
                event.setReportId(report.getId());
                event.setOperatorId(user.getUserId());
                event.setOperatorName(user.getNickname());
                event.setEventTime(LocalDateTime.now());
                event.setCreateTime(LocalDateTime.now());
                event.setUpdateTime(LocalDateTime.now());

                if ("stop".equals(conclusion)) {
                    schedule.setScheduleStatus("injury_stop");
                    schedule.setStopStartDate(request.getStopStartDate());
                    schedule.setResumeDate(null);
                    event.setEventType("injury_stop");
                    event.setEventContent(String.format("工伤停工：自 %s 起停工，预计复工 %s。%s",
                            request.getStopStartDate(),
                            request.getExpectedResumeDate() == null ? "待医嘱" : request.getExpectedResumeDate(),
                            StringUtils.hasText(request.getConclusionRemark()) ? request.getConclusionRemark() : ""));
                } else {
                    schedule.setScheduleStatus("resumed");
                    schedule.setResumeDate(request.getActualResumeDate());
                    event.setEventType("injury_resume");
                    event.setEventContent(String.format("准予复工：复工日期 %s。%s",
                            request.getActualResumeDate(),
                            StringUtils.hasText(request.getConclusionRemark()) ? request.getConclusionRemark() : ""));
                }
                schedule.setUpdateTime(LocalDateTime.now());
                scheduleMapper.updateById(schedule);
                scheduleEventMapper.insert(event);
            }
        }
        return report;
    }

    // ============================ 权限/工具 ============================

    /** 劳务公司可见项目：与其 companyName 匹配的合作项目 */
    private List<Long> projectIdsOfLabor(LoginUser user) {
        if (!StringUtils.hasText(user.getCompanyName())) {
            return Collections.emptyList();
        }
        List<Project> projects = projectMapper.selectList(new LambdaQueryWrapper<Project>()
                .eq(Project::getLaborCompany, user.getCompanyName()));
        return projects.stream().map(Project::getId).collect(Collectors.toList());
    }

    /** 校验当前用户能否查看该项目数据（企业只能看本项目；劳务限本公司合作项目） */
    private void ensureProjectVisible(LoginUser user, Long projectId) {
        if (user == null) {
            throw new BusinessException(401, "未登录或登录失效");
        }
        if ("admin".equals(user.getRole())) {
            return;
        }
        if ("enterprise".equals(user.getRole()) || "supervisor".equals(user.getRole())) {
            if (user.getProjectId() == null || !user.getProjectId().equals(projectId)) {
                throw new BusinessException(403, "无权访问其他项目的工伤数据");
            }
            return;
        }
        if ("labor".equals(user.getRole())) {
            if (!StringUtils.hasText(user.getCompanyName())) {
                throw new BusinessException(403, "当前劳务账号未绑定公司");
            }
            Project project = projectMapper.selectById(projectId);
            if (project == null || !user.getCompanyName().equals(project.getLaborCompany())) {
                throw new BusinessException(403, "该项目不属于当前劳务公司");
            }
        }
    }

    private String generateReportNo() {
        String prefix = "GS" + LocalDateTime.now().format(NO_FMT);
        Long count = this.count(new LambdaQueryWrapper<InjuryReport>()
                .likeRight(InjuryReport::getReportNo, prefix));
        return prefix + String.format("%04d", count + 1);
    }

    private String typeName(String type) {
        if (type == null) {
            return "待分类";
        }
        switch (type) {
            case "outpatient": return "门诊/轻伤";
            case "hospitalized": return "住院";
            case "disability": return "疑似伤残";
            case "death": return "工亡";
            default: return type;
        }
    }
}
