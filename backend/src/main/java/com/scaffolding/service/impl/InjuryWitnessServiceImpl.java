package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.InjuryWitness;
import com.scaffolding.mapper.InjuryWitnessMapper;
import com.scaffolding.service.InjuryWitnessService;
import org.springframework.stereotype.Service;

/**
 * InjuryWitness 服务实现类
 *
 * @author scaffolding
 */
@Service
public class InjuryWitnessServiceImpl extends ServiceImpl<InjuryWitnessMapper, InjuryWitness> implements InjuryWitnessService {
}
