package com.cloud.hospital.system.service;

import com.cloud.hospital.system.common.PageResult;
import com.cloud.hospital.system.dto.AddScheduleDTO;
import com.cloud.hospital.system.dto.ScheduleQueryDTO;
import com.cloud.hospital.system.entity.Schedule;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 医生排班与号源表 服务类
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
public interface IScheduleService extends IService<Schedule> {

    List<Schedule> findSchedules(Long doctorId, LocalDate startDate, LocalDate endDate);

    void addSchedule(AddScheduleDTO addScheduleDTO);

    PageResult<Schedule> pageQuery(ScheduleQueryDTO queryDTO);

    void deleteSchedule(Long scheduleId);

    void updateCapacity(Long id, Integer newTotalNum);

    void restoreSchedule(Long scheduleId);
}
