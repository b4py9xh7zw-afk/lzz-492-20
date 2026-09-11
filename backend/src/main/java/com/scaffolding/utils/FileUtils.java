package com.scaffolding.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件工具类
 * 
 * @author scaffolding
 */
public class FileUtils {

    /**
     * 上传文件
     */
    public static String uploadFile(MultipartFile file, String uploadPath) throws IOException {
        // 创建上传目录
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + extension;

        // 按日期创建子目录
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        File dateDirFile = new File(uploadPath, dateDir);
        if (!dateDirFile.exists()) {
            dateDirFile.mkdirs();
        }

        // 保存文件
        File destFile = new File(dateDirFile, fileName);
        file.transferTo(destFile);

        // 返回相对路径
        return dateDir + "/" + fileName;
    }

    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath, String uploadPath) {
        File file = new File(uploadPath, filePath);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 获取文件类型
     */
    public static String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (extension.matches("jpg|jpeg|png|gif|bmp|webp")) {
            return "image";
        } else if (extension.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt")) {
            return "document";
        } else {
            return "other";
        }
    }
}
