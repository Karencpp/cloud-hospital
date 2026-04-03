package com.cloud.hospital.system.mapper;

import com.cloud.hospital.system.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 医院科室表 Mapper 接口
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    @Update("UPDATE department SET is_deleted = 0 WHERE id = #{id} AND is_deleted = 1")
    int restoreById(@Param("id") Long id);
}
