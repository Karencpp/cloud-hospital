package com.cloud.hospital.system.service;

import com.cloud.hospital.system.common.PageResult;
import com.cloud.hospital.system.dto.AddDepartmentDTO;
import com.cloud.hospital.system.dto.DepartmentQueryDTO;
import com.cloud.hospital.system.dto.UpdateDepartmentDTO;
import com.cloud.hospital.system.entity.Department;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 医院科室表 服务类
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
public interface IDepartmentService extends IService<Department> {

    List<Department> listDepartments();

    Department getDepartment(Long id);

    void addDepartment(AddDepartmentDTO dto);

    void updateDepartment(UpdateDepartmentDTO dto);

    void deleteDepartment(Long id);

    PageResult<Department> pageQuery(DepartmentQueryDTO queryDTO);

    void restoreDepartment(Long id);
}
