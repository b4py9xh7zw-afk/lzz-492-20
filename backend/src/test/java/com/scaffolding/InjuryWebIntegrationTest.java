package com.scaffolding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaffolding.entity.*;
import com.scaffolding.mapper.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 工伤模块 HTTP 接口冒烟：头鉴权、上报、缺件清单、材料上传、企业只读、结论回写
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InjuryWebIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;
    @Autowired private UserMapper userMapper;
    @Autowired private ProjectMapper projectMapper;
    @Autowired private WorkerMapper workerMapper;
    @Autowired private ScheduleMapper scheduleMapper;

    private Long supId, laborId, entId, otherEntId, scheduleId, reportId;
    private Long otherProjectId;

    @BeforeAll
    void seed() {
        Project p = new Project();
        p.setProjectName("WEB测试项目"); p.setLaborCompany("WEB劳务公司");
        projectMapper.insert(p);
        Project p2 = new Project();
        p2.setProjectName("其他项目"); p2.setLaborCompany("别家");
        projectMapper.insert(p2);
        otherProjectId = p2.getId();

        Worker w = new Worker(); w.setWorkerName("WEB工人"); w.setLaborCompany("WEB劳务公司");
        workerMapper.insert(w);
        Schedule s = new Schedule();
        s.setProjectId(p.getId()); s.setWorkerId(w.getId());
        s.setPostName("电焊工"); s.setShift("night");
        s.setScheduleDate(LocalDate.now()); s.setScheduleStatus("normal");
        scheduleMapper.insert(s);
        scheduleId = s.getId();

        supId = user("web_sup", "supervisor", null, p.getId(), null);
        laborId = user("web_labor", "labor", null, null, "WEB劳务公司");
        entId = user("web_ent", "enterprise", null, p.getId(), null);
        otherEntId = user("web_ent2", "enterprise", null, p2.getId(), null);
    }

    private Long user(String name, String role, String company, Long pid, String ignored) {
        User u = new User();
        u.setUsername(name); u.setPassword("x"); u.setNickname(name);
        u.setRole(role); u.setProjectId(pid);
        u.setCompanyName("labor".equals(role) ? "WEB劳务公司" : null);
        userMapper.insert(u);
        return u.getId();
    }

    @Test @Order(1)
    void unauthenticatedIsRejected() throws Exception {
        mockMvc.perform(get("/injury/page"))
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test @Order(2)
    void supervisorSubmitsAndChecklistShowsMissing() throws Exception {
        String body = "{" +
            "\"scheduleId\":" + scheduleId + "," +
            "\"injuryTime\":\"" + LocalDateTime.now().minusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\"," +
            "\"injuryLocation\":\"堆场\"," +
            "\"injuryType\":\"outpatient\"," +
            "\"hospital\":\"市一院\"," +
            "\"submit\":true," +
            "\"witnesses\":[{\"witnessName\":\"王某\",\"witnessPhone\":\"137\",\"witnessType\":\"coworker\"}]}";
        MvcResult mr = mockMvc.perform(post("/injury/report")
                        .header("X-User-Id", supId)
                        .contentType("application/json").content(body))
                .andExpect(jsonPath("$.code").value(200))
                .andReturn();
        JsonNode json = om.readTree(mr.getResponse().getContentAsString());
        reportId = json.get("data").get("id").asLong();

        mockMvc.perform(get("/injury/" + reportId + "/checklist").header("X-User-Id", supId))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.progress").value(0))
                .andExpect(jsonPath("$.data.missingNames[0]").exists());
    }

    @Test @Order(3)
    void supervisorUploadsMaterialAndProgressChanges() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "site.jpg", "image/jpeg", new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/injury/material/upload")
                        .file(file)
                        .param("reportId", String.valueOf(reportId))
                        .param("materialType", "site_photo")
                        .header("X-User-Id", supId))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.requiredProvided").value(1));

        // 主管不能上传劳务类材料
        MockMultipartFile pdf = new MockMultipartFile("file", "c.pdf", "application/pdf", new byte[]{1});
        mockMvc.perform(multipart("/injury/material/upload")
                        .file(pdf)
                        .param("reportId", String.valueOf(reportId))
                        .param("materialType", "labor_contract")
                        .header("X-User-Id", supId))
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test @Order(4)
    void enterpriseIsReadOnlyAndScoped() throws Exception {
        // 本项目可见
        mockMvc.perform(get("/injury/" + reportId).header("X-User-Id", entId))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.canEditInsurance").value(false))
                .andExpect(jsonPath("$.data.canConclude").value(false));

        // 企业上传被拒
        MockMultipartFile f = new MockMultipartFile("file", "x.jpg", "image/jpeg", new byte[]{1});
        mockMvc.perform(multipart("/injury/material/upload").file(f)
                        .param("reportId", String.valueOf(reportId)).param("materialType", "site_photo")
                        .header("X-User-Id", entId))
                .andExpect(jsonPath("$.code").value(403));

        // 其他项目企业不可见
        mockMvc.perform(get("/injury/" + reportId).header("X-User-Id", otherEntId))
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test @Order(5)
    void laborFillsInsuranceAndSupervisorConcludes() throws Exception {
        String ins = "{\"insuranceCompany\":\"人保\",\"policyNo\":\"P1\",\"claimStatus\":\"reported\"}";
        mockMvc.perform(post("/injury/" + reportId + "/insurance")
                        .header("X-User-Id", laborId).contentType("application/json").content(ins))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.policyNo").value("P1"));

        String conclusion = "{\"conclusion\":\"resume\",\"actualResumeDate\":\"" + LocalDate.now() + "\",\"conclusionRemark\":\"愈合复工\"}";
        mockMvc.perform(post("/injury/" + reportId + "/conclusion")
                        .header("X-User-Id", supId).contentType("application/json").content(conclusion))
                .andExpect(jsonPath("$.code").value(200));

        // 排班档案已回写为 resumed
        Schedule sch = scheduleMapper.selectById(scheduleId);
        org.junit.jupiter.api.Assertions.assertEquals("resumed", sch.getScheduleStatus());
        org.junit.jupiter.api.Assertions.assertEquals(reportId, sch.getInjuryReportId());
    }
}
