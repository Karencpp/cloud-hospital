package com.cloud.hospital.system.task;

import com.cloud.hospital.system.constant.RedisKeyPrefix;
import com.cloud.hospital.system.entity.Schedule;
import com.cloud.hospital.system.mapper.ScheduleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * 应用启动时将未来有效排班的号源库存预热到 Redis，
 * 避免上线后第一批请求全部击穿到数据库。
 */
@Slf4j
@Component
public class ScheduleInventoryWarmupTask implements ApplicationRunner {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        log.info(">>> [排班库存预热] 开始将排班余号写入 Redis...");

        // 查询今天及之后、状态正常且有余号的排班
        LambdaQueryWrapper<Schedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Schedule::getWorkDate, LocalDate.now())
                .eq(Schedule::getStatus, (byte) 1)
                .gt(Schedule::getAvailableNum, 0);

        List<Schedule> schedules = scheduleMapper.selectList(wrapper);

        for (Schedule schedule : schedules) {
            String redisKey = RedisKeyPrefix.SCHEDULE_INV_KEY_PREFIX + schedule.getId();
            stringRedisTemplate.opsForValue().set(redisKey, String.valueOf(schedule.getAvailableNum()));
        }

        log.info(">>> [排班库存预热] 完成，共预热 {} 条排班数据", schedules.size());
    }
}
