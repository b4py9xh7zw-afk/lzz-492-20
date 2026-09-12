package com.scaffolding.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scaffolding.common.Result;
import com.scaffolding.dto.injury.MaterialChecklist;
import com.scaffolding.entity.InjuryMaterial;
import com.scaffolding.entity.InjuryReport;
import com.scaffolding.entity.Project;
import com.scaffolding.exception.BusinessException;
import com.scaffolding.mapper.InjuryMaterialMapper;
import com.scaffolding.mapper.ProjectMapper;
import com.scaffolding.security.CurrentUserHolder;
import com.scaffolding.security.LoginUser;
import com.scaffolding.service.InjuryReportService;
import com.scaffolding.service.MaterialCatalog;
import com.scaffolding.utils.FileUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 工伤证明材料控制器
 * <p>
 * 现场证明（照片/证言/事故报告）由主管上传；保险资料（合同/保单/身份证）由劳务上传；
 * 医疗票据双方均可补。企业只读，不能上传/删除。
 *
 * @author scaffolding
 */
@Slf4j
@RestController
@RequestMapping("/injury/material")
@Api(tags = "工伤证明材料")
public class InjuryMaterialController {

    @Autowired
    private InjuryMaterialMapper materialMapper;
    @Autowired
    private InjuryReportService injuryReportService;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private MaterialCatalog materialCatalog;

    @Value("${file.upload.path}")
    private String uploadPath;

    private static final String ALLOW_EXT = "jpg|jpeg|png|gif|bmp|webp|pdf";

    @PostMapping("/upload")
    @ApiOperation("上传工伤证明材料")
    public Result<MaterialChecklist> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("reportId") Long reportId,
            @RequestParam("materialType") String materialType,
            @RequestParam(required = false) String remark) {
        LoginUser user = CurrentUserHolder.require();
        try {
            InjuryReport report = injuryReportService.getById(reportId);
            if (report == null) {
                return Result.error("工伤上报单不存在");
            }
            ensureProjectVisibleStrict(user, report.getProjectId(), projectMapper);

            if ("enterprise".equals(user.getRole())) {
                return Result.error(403, "企业账号仅可查看，不能上传材料");
            }
            if (!materialCatalog.exists(materialType)) {
                return Result.error("未知材料类型：" + materialType);
            }
            String owner = materialCatalog.ownerOf(materialType);
            if ("supervisor".equals(owner) && !("supervisor".equals(user.getRole()) || "admin".equals(user.getRole()))) {
                return Result.error(403, "现场证明材料由主管上传");
            }
            if ("labor".equals(owner) && !("labor".equals(user.getRole()) || "admin".equals(user.getRole()))) {
                return Result.error(403, "保险/用工资料由劳务公司上传");
            }

            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            String original = file.getOriginalFilename();
            if (original == null || !original.contains(".")) {
                return Result.error("文件格式不正确");
            }
            String ext = original.substring(original.lastIndexOf(".") + 1).toLowerCase();
            if (!ext.matches(ALLOW_EXT)) {
                return Result.error("仅支持图片（jpg/png 等）或 PDF 文件");
            }

            String relativePath = FileUtils.uploadFile(file, uploadPath + "/injury");

            InjuryMaterial material = new InjuryMaterial();
            material.setReportId(reportId);
            material.setMaterialType(materialType);
            material.setMaterialName(materialCatalog.nameOf(materialType));
            material.setFileName(relativePath.substring(relativePath.lastIndexOf("/") + 1));
            material.setOriginalName(original);
            material.setFilePath("injury/" + relativePath);
            material.setFileSize(file.getSize());
            material.setFileExt("." + ext);
            material.setOwnerRole(owner);
            material.setUploadUserId(user.getUserId());
            material.setUploadUserName(user.getNickname());
            material.setRemark(remark);
            material.setCreateTime(LocalDateTime.now());
            material.setUpdateTime(LocalDateTime.now());
            materialMapper.insert(material);

            // 实时重算材料完成度并回写主单
            MaterialChecklist checklist = injuryReportService.checklist(reportId);
            report.setMaterialProgress(checklist.getProgress());
            if (checklist.getProgress() == 100 && "reported".equals(report.getReportStatus())) {
                report.setReportStatus("submitted");
            }
            report.setUpdateTime(LocalDateTime.now());
            injuryReportService.updateById(report);

            return Result.success("上传成功", checklist);
        } catch (BusinessException e) {
            return Result.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("上传工伤材料失败", e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除工伤证明材料（企业不可删）")
    public Result<MaterialChecklist> delete(@PathVariable Long id) {
        LoginUser user = CurrentUserHolder.require();
        InjuryMaterial material = materialMapper.selectById(id);
        if (material == null) {
            return Result.error("材料不存在");
        }
        InjuryReport report = injuryReportService.getById(material.getReportId());
        if (report == null) {
            return Result.error("工伤上报单不存在");
        }
        if ("enterprise".equals(user.getRole())) {
            return Result.error(403, "企业账号仅可查看，不能删除材料");
        }
        ensureProjectVisibleStrict(user, report.getProjectId(), projectMapper);

        // 删除物理文件（忽略失败，不阻断记录删除）
        try {
            FileUtils.deleteFile(material.getFilePath(), uploadPath);
        } catch (Exception ignored) {
        }
        materialMapper.deleteById(id);

        MaterialChecklist checklist = injuryReportService.checklist(material.getReportId());
        report.setMaterialProgress(checklist.getProgress());
        if ("submitted".equals(report.getReportStatus()) && checklist.getProgress() < 100) {
            report.setReportStatus("reported");
        }
        report.setUpdateTime(LocalDateTime.now());
        injuryReportService.updateById(report);
        return Result.success("已删除", checklist);
    }

    @GetMapping("/preview/{id}")
    @ApiOperation("预览/下载工伤材料")
    public ResponseEntity<Resource> preview(@PathVariable Long id) {
        InjuryMaterial material = materialMapper.selectById(id);
        if (material == null) {
            return ResponseEntity.notFound().build();
        }
        File file = new File(uploadPath, material.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        String contentType = "application/octet-stream";
        String ext = material.getFileExt() == null ? "" : material.getFileExt().substring(1).toLowerCase();
        if (ext.matches("jpg|jpeg|png|gif|bmp|webp")) {
            contentType = "image/" + ("jpg".equals(ext) ? "jpeg" : ext);
        } else if ("pdf".equals(ext)) {
            contentType = MediaType.APPLICATION_PDF_VALUE;
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\""
                        + URLEncoder.encode(material.getOriginalName(), StandardCharsets.UTF_8) + "\"")
                .body(resource);
    }

    /** 严格项目可见性校验（含劳务合作关系） */
    static void ensureProjectVisibleStrict(LoginUser user, Long projectId, ProjectMapper projectMapper) {
        if (user == null) {
            throw new BusinessException(401, "未登录或登录已失效");
        }
        if ("admin".equals(user.getRole())) {
            return;
        }
        if (("enterprise".equals(user.getRole()) || "supervisor".equals(user.getRole()))) {
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

}
