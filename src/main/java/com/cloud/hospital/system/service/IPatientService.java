package com.cloud.hospital.system.service;

import com.cloud.hospital.system.entity.Patient;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.hospital.system.common.PageResult;
import com.cloud.hospital.system.dto.AddPatientDTO;
import com.cloud.hospital.system.dto.PatientQueryDTO;
import com.cloud.hospital.system.dto.UpdatePatientDTO;

import java.util.List;

/**
 * <p>
 * 患者基础信息表 服务类
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
public interface IPatientService extends IService<Patient> {

    List<Patient> listPatients();

    Patient getPatient(Long id);

    void addPatient(AddPatientDTO dto);

    void updatePatient(UpdatePatientDTO dto);

    void deletePatient(Long id);

    PageResult<Patient> pageQuery(PatientQueryDTO queryDTO);

    void restorePatient(Long id);
}
