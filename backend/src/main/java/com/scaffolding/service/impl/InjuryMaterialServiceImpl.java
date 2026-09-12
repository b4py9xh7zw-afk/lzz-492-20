package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.InjuryMaterial;
import com.scaffolding.mapper.InjuryMaterialMapper;
import com.scaffolding.service.InjuryMaterialService;
import org.springframework.stereotype.Service;

/**
 * InjuryMaterial 服务实现类
 *
 * @author scaffolding
 */
@Service
public class InjuryMaterialServiceImpl extends ServiceImpl<InjuryMaterialMapper, InjuryMaterial> implements InjuryMaterialService {
}
