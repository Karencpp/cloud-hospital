package com.cloud.hospital.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.hospital.system.entity.Schedule;
import com.cloud.hospital.system.mapper.ScheduleMapper;
import com.cloud.hospital.system.service.IScheduleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 医生排班与号源表 服务实现类
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements IScheduleService {

    @Override
    public List<Schedule> findSchedules(Long doctorId, LocalDate startDate, LocalDate endDate) {
        //1.创建Lambda条件构造器
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();

        //2.链式编程构建条件
         wrapper.eq(Schedule :: getDoctorId,doctorId)
                 .ge(Schedule :: getWorkDate,startDate)
                 .le(Schedule :: getWorkDate , endDate)
                 .orderByAsc(Schedule :: getWorkDate)
                 .orderByAsc(Schedule :: getShiftType);

         // 3.执行查询
            return this.list(wrapper);


    }
}
