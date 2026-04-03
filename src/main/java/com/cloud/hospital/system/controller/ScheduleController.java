package com.cloud.hospital.system.controller;

import com.cloud.hospital.system.common.Result;
import com.cloud.hospital.system.dto.AddScheduleDTO;
import com.cloud.hospital.system.dto.ScheduleQueryDTO;
import com.cloud.hospital.system.entity.Schedule;
import com.cloud.hospital.system.service.IScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 医生排班与号源表 前端控制器
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/schedule")
@Tag(name = "2. 排班查询模块", description = "负责医生出诊排班的实时查询")
public class ScheduleController {
    @Autowired
    private IScheduleService scheduleService;
    @GetMapping("/list")
    @Operation(summary = "获取专家排班与余号列表", description = "前端日历组件调用，根据医生ID和日期范围过滤")
    public Result<Object> listSchedules(
            @RequestParam Long doctorId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Schedule> list = scheduleService.findSchedules(doctorId, startDate, endDate);
         log.info("查询排班结果：{}", list);

        // Mock 数据，稍后我们在 Service 实现类里写真正的 LambdaQueryWrapper 查询
        return Result.success(list);
    }
    @PostMapping("/add")
    @Operation(summary = "新增排班", description = "落库保存排班信息，并同步将号源预热至 Redis 中")
    public Result<Void> addSchedule(@RequestBody AddScheduleDTO addScheduleDTO) {
        scheduleService.addSchedule(addScheduleDTO);
        return Result.success(null);
    }
    // ... 之前写的 add 接口 ...

    @PostMapping("/page")
    @Operation(summary = "动态分页查询排班", description = "根据科室、医生、日期等动态条件查询排班列表")
    public Result<Object> pageQuery(@RequestBody ScheduleQueryDTO queryDTO) {
        // 注意：这里的 Result<Object> 最好换成你项目里封装的分页返回对象
        // 如果你还没有封装通用的 PageResult，可以直接返回 MyBatis-Plus 的 Page 对象
        return Result.success(scheduleService.pageQuery(queryDTO));
    }

    @PostMapping("/delete/{id}")
    @Operation(summary = "逻辑删除排班", description = "将is_deleted字段设为 1，并同步删除 Redis 缓存")
    public Result<Void> deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return Result.success(null);
    }

    @PostMapping("/restore/{id}")
    @Operation(summary = "撤销删除排班", description = "将is_deleted字段恢复为 0，并重新预热排班余号到 Redis")
    public Result<Void> restoreSchedule(@PathVariable Long id) {
        scheduleService.restoreSchedule(id);
        return Result.success(null);
    }

    @PostMapping("/update/capacity")
    @Operation(summary = "修改总号源数", description = "更新排班总号源，并根据预约情况同步更新可用号源和 Redis 缓存")
    public Result<Void> updateCapacity(
            @RequestParam Long id,
            @RequestParam Integer newTotalNum) {
        scheduleService.updateCapacity(id, newTotalNum);
        return Result.success(null);
    }
}
