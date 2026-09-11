package com.scaffolding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scaffolding.entity.Work;
import org.apache.ibatis.annotations.Mapper;

/**
 * 工作管理Mapper接口
 * 
 * @author scaffolding
 */
@Mapper
public interface WorkMapper extends BaseMapper<Work> {
}
