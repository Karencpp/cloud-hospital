package com.cloud.hospital.system.mapper;

import com.cloud.hospital.system.entity.Schedule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 医生排班与号源表 Mapper 接口
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

}
