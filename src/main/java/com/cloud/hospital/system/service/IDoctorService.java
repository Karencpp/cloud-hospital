package com.cloud.hospital.system.service;

import com.cloud.hospital.system.entity.Doctor;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.hospital.system.common.PageResult;
import com.cloud.hospital.system.dto.AddDoctorDTO;
import com.cloud.hospital.system.dto.DoctorQueryDTO;
import com.cloud.hospital.system.dto.UpdateDoctorDTO;

import java.util.List;

/**
 * <p>
 * 医生信息表 服务类
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
public interface IDoctorService extends IService<Doctor> {

    List<Doctor> listDoctors(Long departmentId);

    Doctor getDoctor(Long id);

    void addDoctor(AddDoctorDTO dto);

    void updateDoctor(UpdateDoctorDTO dto);

    void deleteDoctor(Long id);

    PageResult<Doctor> pageQuery(DoctorQueryDTO queryDTO);

    void restoreDoctor(Long id);
}
