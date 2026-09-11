package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Work;
import com.scaffolding.mapper.WorkMapper;
import com.scaffolding.service.WorkService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 工作管理服务实现类
 * 
 * @author scaffolding
 */
@Service
public class WorkServiceImpl extends ServiceImpl<WorkMapper, Work> implements WorkService {

    @Override
    public Page<Work> pageQuery(Long current, Long size, String workName, String workStatus, String priority) {
        Page<Work> page = new Page<>(current, size);
        LambdaQueryWrapper<Work> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(workName)) {
            wrapper.like(Work::getWorkName, workName);
        }
        if (StringUtils.hasText(workStatus)) {
            wrapper.eq(Work::getWorkStatus, workStatus);
        }
        if (StringUtils.hasText(priority)) {
            wrapper.eq(Work::getPriority, priority);
        }
        
        wrapper.orderByDesc(Work::getCreateTime);
        return this.page(page, wrapper);
    }
}
