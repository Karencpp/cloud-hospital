package com.cloud.hospital.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.hospital.system.common.PageResult;
import com.cloud.hospital.system.constant.RedisKeyPrefix;
import com.cloud.hospital.system.dto.AddScheduleDTO;
import com.cloud.hospital.system.dto.ScheduleQueryDTO;
import com.cloud.hospital.system.entity.Schedule;
import com.cloud.hospital.system.mapper.ScheduleMapper;
import com.cloud.hospital.system.service.IScheduleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.hospital.system.task.ScheduleInventoryWarmupTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 医生排班与号源表 服务实现类
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 *
 *
 */
@Slf4j
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements IScheduleService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSchedule(AddScheduleDTO dto) {
          //1.基础业务校验: 不能给过去的日期排班
          if(dto.getWorkDate().isBefore(LocalDate.now())){
              throw new RuntimeException("排班日期不能早于今天");
          }

          //2. 防重校验: 同一个医生,同一天,同一个班次,不能排两次

        long count = this.count(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule :: getDoctorId, dto.getDoctorId())
                .eq(Schedule :: getWorkDate, dto.getWorkDate())
                .eq(Schedule:: getShiftType, dto.getShiftType()));

          if(count>0){
              throw new RuntimeException("同一医生,同一天,同一个班次,不能排两次");
          }
                //3.构建实体类并初始化关键字段
        Schedule schedule = new Schedule();
          BeanUtils.copyProperties(dto,schedule);
          schedule.setAvailableNum(dto.getTotalNum());

          this.save(schedule);

          //同时更新Redis缓存

          String RedisKey = RedisKeyPrefix.SCHEDULE_INV_KEY_PREFIX+schedule.getId();

          stringRedisTemplate.opsForValue().set(RedisKey, String.valueOf(schedule.getAvailableNum()));

          log.info("新增排班信息成功,排班id为:{}",schedule.getId());

    }

    @Override
    public PageResult<Schedule> pageQuery(ScheduleQueryDTO queryDTO) {
        Page<Schedule> pageParam = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(queryDTO.getDoctorId()!=null,Schedule:: getDoctorId,queryDTO.getDoctorId());
        wrapper.eq(queryDTO.getDepartmentId()!=null,Schedule:: getDepartmentId,queryDTO.getDepartmentId());
        wrapper.ge(queryDTO.getStartDate()!=null,Schedule :: getWorkDate,queryDTO.getStartDate());
        wrapper.eq(Schedule :: getStatus,(byte)1);
        if(Boolean.TRUE.equals(queryDTO.getOnlyAvailable())){
            wrapper.gt(Schedule:: getAvailableNum,0);
        }
        wrapper.orderByAsc(Schedule:: getWorkDate).orderByAsc(Schedule:: getShiftType);

        Page<Schedule> rawPage = this.page(pageParam,wrapper);

        return new PageResult<>(rawPage.getTotal(), rawPage.getPages(), rawPage.getRecords());
    }

    public Schedule getScheduleWithCache(Long scheduleId) {
        // 1. 查数据库，获取基础排班信息
        Schedule schedule = this.getById(scheduleId);
        if (schedule == null) {
            return null;
        }

        // 2. 拼装 Redis Key
        String redisKey = RedisKeyPrefix.SCHEDULE_INV_KEY_PREFIX + scheduleId;

        // 3. 从 Redis 中获取最新余号
        String invStr = stringRedisTemplate.opsForValue().get(redisKey);

        if (invStr != null) {
            // 如果 Redis 里有数据，以 Redis 为准，覆盖实体类里的旧数据
            schedule.setAvailableNum(Integer.parseInt(invStr));
        } else {
            // 【大厂兜底逻辑】：万一 Redis 宕机或者数据过期丢失了怎么办？
            // 此时以数据库的数据为准，并可以考虑在这里把数据重新塞回 Redis (缓存击穿的防御)
            log.warn(">>> [排班查询] Redis中未找到排班余号缓存，使用数据库兜底数据，scheduleId: {}", scheduleId);
            stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(schedule.getAvailableNum()));
        }

        return schedule;
    }
}
