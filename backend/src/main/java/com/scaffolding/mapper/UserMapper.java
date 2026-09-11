package com.scaffolding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scaffolding.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 * 
 * @author scaffolding
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
