package com.scaffolding.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scaffolding.entity.ScheduleEvent;
import com.scaffolding.mapper.ScheduleEventMapper;
import com.scaffolding.service.ScheduleEventService;
import org.springframework.stereotype.Service;

/**
 * ScheduleEvent 服务实现类
 *
 * @author scaffolding
 */
@Service
public class ScheduleEventServiceImpl extends ServiceImpl<ScheduleEventMapper, ScheduleEvent> implements ScheduleEventService {
}
