package com.cloud.hospital.system.controller;

import com.cloud.hospital.system.common.Result;
import com.cloud.hospital.system.entity.Schedule;
import com.cloud.hospital.system.service.IScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
