package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.Schedule;
import com.scaffolding.mapper.ScheduleMapper;
import com.scaffolding.service.ScheduleService;
import org.springframework.stereotype.Service;

/**
 * Schedule 服务实现类
 *
 * @author scaffolding
 */
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {
}
