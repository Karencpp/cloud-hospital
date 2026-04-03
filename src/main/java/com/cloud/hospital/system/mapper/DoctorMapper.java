package com.cloud.hospital.system.mapper;

import com.cloud.hospital.system.entity.Doctor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

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

    @Update("UPDATE doctor SET is_deleted = 0 WHERE id = #{id} AND is_deleted = 1")
    int restoreById(@Param("id") Long id);
}
