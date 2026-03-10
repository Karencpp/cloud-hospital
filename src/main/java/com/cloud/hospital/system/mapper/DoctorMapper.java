package com.cloud.hospital.system.mapper;

import com.cloud.hospital.system.entity.Doctor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 医生信息表 Mapper 接口
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

}
