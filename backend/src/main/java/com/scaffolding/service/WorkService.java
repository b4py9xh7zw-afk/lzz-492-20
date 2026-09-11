package com.scaffolding.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scaffolding.entity.Work;

/**
 * 工作管理服务接口
 * 
 * @author scaffolding
 */
public interface WorkService extends IService<Work> {

    /**
     * 分页查询工作信息
     */
    Page<Work> pageQuery(Long current, Long size, String workName, String workStatus, String priority);
}
