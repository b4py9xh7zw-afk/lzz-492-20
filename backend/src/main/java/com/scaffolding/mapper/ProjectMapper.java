package com.scaffolding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scaffolding.entity.Project;
import org.apache.ibatis.annotations.Mapper;

/**
 * Project Mapper接口
 *
 * @author scaffolding
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
