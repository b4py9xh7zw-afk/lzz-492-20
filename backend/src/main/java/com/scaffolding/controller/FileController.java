package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.FileInfo;
import com.scaffolding.service.FileInfoService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 文件管理控制器
 * 
 * @author scaffolding
 */
@Slf4j
@RestController
@RequestMapping("/file")
@Api(tags = "文件管理")
public class FileController {

    @Autowired
    private FileInfoService fileInfoService;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url}")
    private String fileUrl;

    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public Result<FileInfo> upload(@RequestParam("file") MultipartFile file,
                                    @RequestParam(required = false, defaultValue = "系统") String uploadUserName,
                                    @RequestParam(required = false, defaultValue = "1") Long uploadUserId) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }

            // 检查文件类型，只允许图片
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                return Result.error("文件格式不正确");
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!extension.matches("jpg|jpeg|png|gif|bmp|webp")) {
                return Result.error("只能上传图片文件（jpg、jpeg、png、gif、bmp、webp）");
            }

            // 上传文件
            String filePath = FileUtils.uploadFile(file, uploadPath);

            // 保存文件信息
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(file.getOriginalFilename());
            fileInfo.setOriginalName(file.getOriginalFilename());
            fileInfo.setFilePath(filePath);
            fileInfo.setFileSize(file.getSize());
            fileInfo.setFileType(FileUtils.getFileType(file.getOriginalFilename()));
            fileInfo.setFileExtension(file.getOriginalFilename() != null && file.getOriginalFilename().contains(".") 
                    ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")) : "");
            fileInfo.setUploadUserId(uploadUserId);
            fileInfo.setUploadUserName(uploadUserName);
            fileInfo.setCreateTime(LocalDateTime.now());
            fileInfo.setUpdateTime(LocalDateTime.now());

            fileInfoService.save(fileInfo);

            // 设置访问URL（用于返回给前端）
            String previewUrl = fileUrl + "/preview/" + fileInfo.getId();
            fileInfo.setFilePath(previewUrl);

            return Result.success("上传成功", fileInfo);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    @GetMapping("/preview/{id}")
    @ApiOperation("预览文件")
    public ResponseEntity<Resource> preview(@PathVariable Long id) {
        FileInfo fileInfo = fileInfoService.getById(id);
        if (fileInfo == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(uploadPath, fileInfo.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String contentType = "application/octet-stream";
        if (fileInfo.getFileType().equals("image")) {
            contentType = "image/" + fileInfo.getFileExtension().substring(1);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + 
                        URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8) + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除文件")
    public Result<?> delete(@PathVariable Long id) {
        FileInfo fileInfo = fileInfoService.getById(id);
        if (fileInfo == null) {
            return Result.error("文件不存在");
        }

        // 删除物理文件
        FileUtils.deleteFile(fileInfo.getFilePath(), uploadPath);

        // 删除数据库记录
        fileInfoService.removeById(id);

        return Result.success("删除成功");
    }

    @GetMapping("/page")
    @ApiOperation("分页查询文件")
    public Result<PageResult<FileInfo>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String fileType) {
        Page<FileInfo> page = fileInfoService.pageQuery(current, size, fileName, fileType);
        
        // 设置访问URL
        page.getRecords().forEach(fileInfo -> {
            fileInfo.setFilePath(fileUrl + "/preview/" + fileInfo.getId());
        });

        PageResult<FileInfo> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }
}
