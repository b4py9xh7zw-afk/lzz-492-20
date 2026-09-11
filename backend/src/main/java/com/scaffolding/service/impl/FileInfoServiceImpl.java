package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.FileInfo;
import com.scaffolding.mapper.FileInfoMapper;
import com.scaffolding.service.FileInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 文件信息服务实现类
 * 
 * @author scaffolding
 */
@Service
public class FileInfoServiceImpl extends ServiceImpl<FileInfoMapper, FileInfo> implements FileInfoService {

    @Override
    public Page<FileInfo> pageQuery(Long current, Long size, String fileName, String fileType) {
        Page<FileInfo> page = new Page<>(current, size);
        LambdaQueryWrapper<FileInfo> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(fileName)) {
            wrapper.like(FileInfo::getFileName, fileName);
        }
        if (StringUtils.hasText(fileType)) {
            wrapper.eq(FileInfo::getFileType, fileType);
        }
        
        wrapper.orderByDesc(FileInfo::getCreateTime);
        return this.page(page, wrapper);
    }
}
