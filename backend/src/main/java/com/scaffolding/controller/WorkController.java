package com.scaffolding.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scaffolding.common.PageResult;
import com.scaffolding.common.Result;
import com.scaffolding.entity.Work;
import com.scaffolding.service.WorkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 工作管理控制器
 * 
 * @author scaffolding
 */
@Slf4j
@RestController
@RequestMapping("/work")
@Api(tags = "工作管理")
public class WorkController {

    @Autowired
    private WorkService workService;

    @PostMapping
    @ApiOperation("新增工作")
    public Result<Work> save(@RequestBody Work work) {
        try {
            if (work.getWorkTime() == null) {
                work.setWorkTime(LocalDateTime.now());
            }
            work.setCreateTime(LocalDateTime.now());
            work.setUpdateTime(LocalDateTime.now());
            workService.save(work);
            return Result.success("新增成功", work);
        } catch (Exception e) {
            log.error("新增工作失败", e);
            return Result.error("新增失败：" + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation("更新工作")
    public Result<Work> update(@PathVariable Long id, @RequestBody Work work) {
        try {
            work.setId(id);
            work.setUpdateTime(LocalDateTime.now());
            workService.updateById(work);
            return Result.success("更新成功", work);
        } catch (Exception e) {
            log.error("更新工作失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除工作")
    public Result<?> delete(@PathVariable Long id) {
        try {
            workService.removeById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            log.error("删除工作失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID查询工作")
    public Result<Work> getById(@PathVariable Long id) {
        Work work = workService.getById(id);
        if (work == null) {
            return Result.error("工作不存在");
        }
        return Result.success(work);
    }

    @GetMapping("/page")
    @ApiOperation("分页查询工作")
    public Result<PageResult<Work>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String workName,
            @RequestParam(required = false) String workStatus,
            @RequestParam(required = false) String priority) {
        Page<Work> page = workService.pageQuery(current, size, workName, workStatus, priority);
        
        PageResult<Work> pageResult = new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                page.getCurrent(),
                page.getSize()
        );
        return Result.success(pageResult);
    }
}
