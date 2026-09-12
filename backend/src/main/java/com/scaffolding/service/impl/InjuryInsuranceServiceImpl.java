package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.InjuryInsurance;
import com.scaffolding.mapper.InjuryInsuranceMapper;
import com.scaffolding.service.InjuryInsuranceService;
import org.springframework.stereotype.Service;

/**
 * InjuryInsurance 服务实现类
 *
 * @author scaffolding
 */
@Service
public class InjuryInsuranceServiceImpl extends ServiceImpl<InjuryInsuranceMapper, InjuryInsurance> implements InjuryInsuranceService {
}
