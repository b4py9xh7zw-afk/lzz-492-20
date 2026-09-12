package com.scaffolding.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scaffolding.common.Result;
import com.scaffolding.entity.*;
import com.scaffolding.security.CurrentUserHolder;
import com.scaffolding.security.LoginUser;
import com.scaffolding.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 排班档案 / 基础数据控制器
 * <p>
 * 提供主管手机端选岗位班次所需的项目、工人、排班档案数据，全部按角色做隔离。
 *
 * @author scaffolding
 */
@Slf4j
@RestController
@RequestMapping("/schedule")
@Api(tags = "排班档案与基础数据")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private WorkerService workerService;
    @Autowired
    private ScheduleEventService scheduleEventService;

    @GetMapping("/projects")
    @ApiOperation("可见项目列表（按角色隔离）")
    public Result<List<Project>> projects() {
        LoginUser user = CurrentUserHolder.require();
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if ("enterprise".equals(user.getRole()) || "supervisor".equals(user.getRole())) {
            wrapper.eq(Project::getId, user.getProjectId());
        } else if ("labor".equals(user.getRole()) && StringUtils.hasText(user.getCompanyName())) {
            wrapper.eq(Project::getLaborCompany, user.getCompanyName());
        }
        wrapper.orderByDesc(Project::getId);
        return Result.success(projectService.list(wrapper));
    }

    @GetMapping("/workers")
    @ApiOperation("工人列表（可按劳务公司/关键字过滤）")
    public Result<List<Worker>> workers(@RequestParam(required = false) String keyword) {
        LoginUser user = CurrentUserHolder.require();
        LambdaQueryWrapper<Worker> wrapper = new LambdaQueryWrapper<>();
        if ("labor".equals(user.getRole()) && StringUtils.hasText(user.getCompanyName())) {
            wrapper.eq(Worker::getLaborCompany, user.getCompanyName());
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Worker::getWorkerName, keyword).or().like(Worker::getPhone, keyword));
        }
        wrapper.orderByDesc(Worker::getId);
        return Result.success(workerService.list(wrapper));
    }

    @GetMapping("/list")
    @ApiOperation("排班档案列表（可按单日或起止日期查询，支持补报历史工伤）")
    public Result<List<Schedule>> list(@RequestParam(required = false) Long projectId,
                                       @RequestParam(required = false) Long workerId,
                                       @RequestParam(required = false) String date,
                                       @RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        LoginUser user = CurrentUserHolder.require();
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();

        if ("enterprise".equals(user.getRole()) || "supervisor".equals(user.getRole())) {
            wrapper.eq(Schedule::getProjectId, user.getProjectId());
        } else if ("labor".equals(user.getRole())) {
            List<Project> projects = projectService.list(new LambdaQueryWrapper<Project>()
                    .eq(StringUtils.hasText(user.getCompanyName()), Project::getLaborCompany, user.getCompanyName()));
            List<Long> ids = new ArrayList<>();
            projects.forEach(p -> ids.add(p.getId()));
            if (ids.isEmpty()) {
                return Result.success(new ArrayList<>());
            }
            wrapper.in(Schedule::getProjectId, ids);
        }

        wrapper.eq(projectId != null, Schedule::getProjectId, projectId)
                .eq(workerId != null, Schedule::getWorkerId, workerId)
                .eq(StringUtils.hasText(date), Schedule::getScheduleDate,
                        StringUtils.hasText(date) ? LocalDate.parse(date) : null)
                .ge(StringUtils.hasText(startDate), Schedule::getScheduleDate,
                        StringUtils.hasText(startDate) ? LocalDate.parse(startDate) : null)
                .le(StringUtils.hasText(endDate), Schedule::getScheduleDate,
                        StringUtils.hasText(endDate) ? LocalDate.parse(endDate) : null)
                .orderByDesc(Schedule::getScheduleDate)
                .orderByAsc(Schedule::getShift);
        return Result.success(scheduleService.list(wrapper));
    }

    @GetMapping("/{id}")
    @ApiOperation("排班档案详情（含工伤事件时间线）")
    public Result<Schedule> detail(@PathVariable Long id) {
        Schedule schedule = scheduleService.getById(id);
        if (schedule == null) {
            return Result.error("排班档案不存在");
        }
        LoginUser user = CurrentUserHolder.require();
        if (("enterprise".equals(user.getRole()) || "supervisor".equals(user.getRole()))
                && (user.getProjectId() == null || !user.getProjectId().equals(schedule.getProjectId()))) {
            return Result.error(403, "无权访问其他项目的排班档案");
        }
        return Result.success(schedule);
    }

    @GetMapping("/{id}/events")
    @ApiOperation("排班档案工伤事件时间线（上报/停工/复工留痕）")
    public Result<List<ScheduleEvent>> events(@PathVariable Long id) {
        LoginUser user = CurrentUserHolder.require();
        Schedule schedule = scheduleService.getById(id);
        if (schedule == null) {
            return Result.error("排班档案不存在");
        }
        if (("enterprise".equals(user.getRole()) || "supervisor".equals(user.getRole()))
                && (user.getProjectId() == null || !user.getProjectId().equals(schedule.getProjectId()))) {
            return Result.error(403, "无权访问其他项目的排班档案");
        }
        return Result.success(scheduleEventService.list(new LambdaQueryWrapper<ScheduleEvent>()
                .eq(ScheduleEvent::getScheduleId, id)
                .orderByDesc(ScheduleEvent::getEventTime)
                .orderByDesc(ScheduleEvent::getId)));
    }
}
