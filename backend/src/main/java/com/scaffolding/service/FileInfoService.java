package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.FileInfo;

/**
 * 文件信息服务接口
 * 
 * @author scaffolding
 */
public interface FileInfoService extends IService<FileInfo> {

    /**
     * 分页查询文件信息
     */
    Page<FileInfo> pageQuery(Long current, Long size, String fileName, String fileType);
}
