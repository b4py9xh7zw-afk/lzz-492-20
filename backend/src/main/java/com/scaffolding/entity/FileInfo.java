package com.scaffolding.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件信息实体类
 * 
 * @author scaffolding
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_info")
public class FileInfo extends BaseEntity {

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件扩展名
     */
    private String fileExtension;

    /**
     * 上传人ID
     */
    private Long uploadUserId;

    /**
     * 上传人姓名
     */
    private String uploadUserName;
}
