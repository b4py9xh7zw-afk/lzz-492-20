package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Project;
import com.scaffolding.mapper.ProjectMapper;
import com.scaffolding.service.ProjectService;
import org.springframework.stereotype.Service;

/**
 * Project 服务实现类
 *
 * @author scaffolding
 */
@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {
}
