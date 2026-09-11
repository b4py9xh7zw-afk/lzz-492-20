package com.scaffolding.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scaffolding.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件信息Mapper接口
 * 
 * @author scaffolding
 */
@Mapper
public interface FileInfoMapper extends BaseMapper<FileInfo> {
}
