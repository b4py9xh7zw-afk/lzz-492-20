package com.scaffolding;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scaffolding.dto.injury.*;
import com.scaffolding.entity.*;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.*;
import com.scaffolding.security.CurrentUserHolder;
import com.scaffolding.security.LoginUser;
import com.scaffolding.service.InjuryReportService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工伤上报材料包冒烟测试（H2 MySQL 模式，无需外部数据库）
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(Lifecycle.PER_CLASS)
class InjuryModuleIntegrationTest {

    @Autowired private InjuryReportService injuryService;
    @Autowired private UserMapper userMapper;
    @Autowired private ProjectMapper projectMapper;
    @Autowired private WorkerMapper workerMapper;
    @Autowired private ScheduleMapper scheduleMapper;
    @Autowired private InjuryWitnessMapper witnessMapper;
    @Autowired private InjuryMaterialMapper materialMapper;
    @Autowired private ScheduleEventMapper eventMapper;

    private static Long supervisorId, laborId, enterpriseId, otherEnterpriseId;
    private static Long projectId, workerId, scheduleId, otherProjectId;
    private static Long reportId;

    @BeforeAll
    void seed() {
        Project p = new Project();
        p.setProjectName("测试会展项目");
        p.setEnterpriseName("测试企业");
        p.setLaborCompany("测试劳务公司");
        p.setStatus("active");
        projectMapper.insert(p);
        projectId = p.getId();

        Project p2 = new Project();
        p2.setProjectName("其他企业项目");
        p2.setEnterpriseName("别家企业");
        p2.setLaborCompany("别家劳务");
        p2.setStatus("active");
        projectMapper.insert(p2);
        otherProjectId = p2.getId();

        Worker w = new Worker();
        w.setWorkerName("测试工人");
        w.setPhone("13800000000");
        w.setLaborCompany("测试劳务公司");
        workerMapper.insert(w);
        workerId = w.getId();

        Schedule sch = new Schedule();
        sch.setProjectId(projectId);
        sch.setWorkerId(workerId);
        sch.setPostName("架子工");
        sch.setShift("day");
        sch.setScheduleDate(LocalDate.now());
        sch.setStartTime(LocalDateTime.now().minusHours(3));
        sch.setScheduleStatus("normal");
        scheduleMapper.insert(sch);
        scheduleId = sch.getId();

        supervisorId = createUser("sup_test", "supervisor", "刘主管", projectId, null);
        laborId = createUser("labor_test", "labor", "陈经办", null, "测试劳务公司");
        enterpriseId = createUser("ent_test", "enterprise", "安全科", projectId, null);
        otherEnterpriseId = createUser("ent_other", "enterprise", "别家安全科", otherProjectId, null);
    }

    private Long createUser(String username, String role, String nick, Long projectId, String company) {
        User u = new User();
        u.setUsername(username);
        u.setPassword("123456");
        u.setNickname(nick);
        u.setRole(role);
        u.setProjectId(projectId);
        u.setCompanyName(company);
        userMapper.insert(u);
        return u.getId();
    }

    private void login(Long id, String role, String nick, Long projectId, String company) {
        CurrentUserHolder.set(new LoginUser(id, nick, role, projectId, company));
    }

    @AfterAll
    void clear() {
        CurrentUserHolder.clear();
    }

    @Test
    @Order(1)
    void supervisorCanSubmitReportAndScheduleGetsEvent() {
        login(supervisorId, "supervisor", "刘主管", projectId, null);

        InjuryReportSaveRequest req = new InjuryReportSaveRequest();
        req.setScheduleId(scheduleId);
        req.setInjuryTime(LocalDateTime.now().minusHours(2));
        req.setInjuryLocation("3#楼6层外架");
        req.setInjuryType("hospitalized");
        req.setInjuryDesc("踩空跌落，左手腕骨折");
        req.setHospital("市第七人民医院");
        req.setHospitalTime(LocalDateTime.now().minusHours(1));
        req.setSubmit(true);
        InjuryWitness witness = new InjuryWitness();
        witness.setWitnessName("同班组老李");
        witness.setWitnessPhone("13700000000");
        req.setWitnesses(Collections.singletonList(witness));

        InjuryReport report = injuryService.saveReport(req);
        reportId = report.getId();

        assertNotNull(report.getReportNo());
        assertTrue(report.getReportNo().startsWith("GS"));
        assertEquals("reported", report.getReportStatus());
        assertEquals("架子工", report.getPostName());
        assertEquals("day", report.getShiftName());

        // 见证人已保存
        Long witnessCount = witnessMapper.selectCount(new LambdaQueryWrapper<InjuryWitness>()
                .eq(InjuryWitness::getReportId, reportId));
        assertEquals(1L, witnessCount);

        // 排班档案已挂工伤单 + 上报事件留痕
        Schedule sch = scheduleMapper.selectById(scheduleId);
        assertEquals(reportId, sch.getInjuryReportId());
        Long eventCount = eventMapper.selectCount(new LambdaQueryWrapper<ScheduleEvent>()
                .eq(ScheduleEvent::getScheduleId, scheduleId)
                .eq(ScheduleEvent::getEventType, "injury_report"));
        assertEquals(1L, eventCount);
    }

    @Test
    @Order(2)
    void checklistShowsMissingMaterials() {
        login(supervisorId, "supervisor", "刘主管", projectId, null);
        MaterialChecklist checklist = injuryService.checklist(reportId);

        // 住院类型必需 10 项，当前 0 上传
        assertTrue(checklist.getRequiredTotal() >= 8);
        assertEquals(0, checklist.getRequiredProvided());
        assertEquals(0, checklist.getProgress());
        assertTrue(checklist.getMissingNames().contains("事故现场照片/视频截图"));
        assertTrue(checklist.getMissingSite().contains("事故经过书面报告"));
        assertTrue(checklist.getMissingInsurance().contains("参保证明/保单"));
        assertTrue(checklist.getMissingInsurance().contains("劳动合同/用工关系证明"));
        assertTrue(checklist.getMissingMedical().contains("住院病历/出院小结"));
    }

    @Test
    @Order(3)
    void enterpriseCannotSubmit() {
        login(enterpriseId, "enterprise", "安全科", projectId, null);
        InjuryReportSaveRequest req = new InjuryReportSaveRequest();
        req.setScheduleId(scheduleId);
        req.setInjuryTime(LocalDateTime.now());
        req.setInjuryLocation("x");
        BusinessException ex = assertThrows(BusinessException.class, () -> injuryService.saveReport(req));
        assertEquals(Integer.valueOf(403), ex.getCode());
    }

    @Test
    @Order(4)
    void enterpriseFromOtherProjectForbidden() {
        login(otherEnterpriseId, "enterprise", "别家安全科", otherProjectId, null);
        BusinessException ex = assertThrows(BusinessException.class, () -> injuryService.detail(reportId));
        assertEquals(Integer.valueOf(403), ex.getCode());

        // 列表也看不到别的项目
        var page = injuryService.pageQuery(1L, 10L, null, null, null);
        assertEquals(0L, page.getTotal());
    }

    @Test
    @Order(5)
    void laborOfOwnCompanyCanSeeAndFillInsuranceButNotOtherCompany() {
        // 别家劳务不可见
        LoginUser wrongLabor = new LoginUser(9999L, "别家", "labor", null, "别家劳务");
        CurrentUserHolder.set(wrongLabor);
        assertThrows(BusinessException.class, () -> injuryService.detail(reportId));

        // 本项目劳务可见
        login(laborId, "labor", "陈经办", null, "测试劳务公司");
        InjuryReportDetail detail = injuryService.detail(reportId);
        assertTrue(detail.getCanEditInsurance());

        InjuryInsurance ins = new InjuryInsurance();
        ins.setInsuranceCompany("人保财险");
        ins.setPolicyNo("PZBG-001");
        ins.setClaimStatus("reported");
        ins.setContactName("陈经办");
        InjuryInsurance saved = injuryService.upsertInsurance(reportId, ins);
        assertEquals("PZBG-001", saved.getPolicyNo());
        assertEquals(reportId, saved.getReportId());

        // 劳务不能下结论
        ConclusionRequest c = new ConclusionRequest();
        c.setConclusion("stop");
        c.setStopStartDate(LocalDate.now());
        BusinessException ex = assertThrows(BusinessException.class, () -> injuryService.conclude(reportId, c));
        assertEquals(Integer.valueOf(403), ex.getCode());
    }

    @Test
    @Order(6)
    void uploadedMaterialUpdatesProgress() {
        login(supervisorId, "supervisor", "刘主管", projectId, null);
        // 模拟主管已上传现场照片、事故报告；劳务类材料仍缺
        for (String type : new String[]{"site_photo", "accident_report"}) {
            InjuryMaterial m = new InjuryMaterial();
            m.setReportId(reportId);
            m.setMaterialType(type);
            m.setMaterialName(type);
            m.setOwnerRole("supervisor");
            materialMapper.insert(m);
        }
        MaterialChecklist checklist = injuryService.checklist(reportId);
        assertEquals(2, checklist.getRequiredProvided());
        assertTrue(checklist.getProgress() > 0);
        assertFalse(checklist.getMissingNames().contains("事故现场照片/视频截图"));
    }


    @Test
    @Order(8)
    void draftThenSubmitAlsoWritesBackToSchedule() {
        login(supervisorId, "supervisor", "刘主管", projectId, null);

        Worker w2 = new Worker();
        w2.setWorkerName("草稿工人");
        w2.setLaborCompany("测试劳务公司");
        workerMapper.insert(w2);

        Schedule sch2 = new Schedule();
        sch2.setProjectId(projectId);
        sch2.setWorkerId(w2.getId());
        sch2.setPostName("普工");
        sch2.setShift("night");
        sch2.setScheduleDate(LocalDate.now());
        sch2.setScheduleStatus("normal");
        scheduleMapper.insert(sch2);

        InjuryReportSaveRequest draft = new InjuryReportSaveRequest();
        draft.setScheduleId(sch2.getId());
        draft.setInjuryTime(LocalDateTime.now().minusHours(1));
        draft.setInjuryLocation("材料堆场");
        draft.setInjuryType("outpatient");
        draft.setSubmit(false);
        InjuryReport d = injuryService.saveReport(draft);

        Schedule before = scheduleMapper.selectById(sch2.getId());
        assertNull(before.getInjuryReportId());

        draft.setId(d.getId());
        draft.setHospital("市一院");
        InjuryWitness wit = new InjuryWitness();
        wit.setWitnessName("见证赵某");
        draft.setWitnesses(Collections.singletonList(wit));
        draft.setSubmit(true);
        injuryService.saveReport(draft);

        Schedule after = scheduleMapper.selectById(sch2.getId());
        assertEquals(d.getId(), after.getInjuryReportId());
        assertEquals("injury_stop", after.getScheduleStatus());
        Long events = eventMapper.selectCount(new LambdaQueryWrapper<ScheduleEvent>()
                .eq(ScheduleEvent::getScheduleId, sch2.getId())
                .eq(ScheduleEvent::getEventType, "injury_report"));
        assertEquals(1L, events);
    }

    @Test
    @Order(7)
    void stopThenResumeWritesBackToSchedule() {
        login(supervisorId, "supervisor", "刘主管", projectId, null);

        // 停工
        ConclusionRequest stop = new ConclusionRequest();
        stop.setConclusion("stop");
        stop.setStopStartDate(LocalDate.now());
        stop.setExpectedResumeDate(LocalDate.now().plusWeeks(6));
        stop.setConclusionRemark("医嘱制动休息6周");
        injuryService.conclude(reportId, stop);

        Schedule stopped = scheduleMapper.selectById(scheduleId);
        assertEquals("injury_stop", stopped.getScheduleStatus());
        assertEquals("stop", stopped.getConclusion());
        assertNotNull(stopped.getStopStartDate());

        // 复工
        ConclusionRequest resume = new ConclusionRequest();
        resume.setConclusion("resume");
        resume.setActualResumeDate(LocalDate.now().plusWeeks(6));
        resume.setConclusionRemark("复诊愈合，准予复工");
        injuryService.conclude(reportId, resume);

        Schedule resumed = scheduleMapper.selectById(scheduleId);
        assertEquals("resumed", resumed.getScheduleStatus());
        assertEquals("resume", resumed.getConclusion());
        assertNotNull(resumed.getResumeDate());

        // 三类事件齐全
        Long total = eventMapper.selectCount(new LambdaQueryWrapper<ScheduleEvent>()
                .eq(ScheduleEvent::getScheduleId, scheduleId));
        assertEquals(3L, total);

        InjuryReport report = injuryService.getById(reportId);
        assertEquals("concluded", report.getReportStatus());
    }
}
