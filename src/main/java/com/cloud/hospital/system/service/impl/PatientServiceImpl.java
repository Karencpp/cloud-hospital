package com.cloud.hospital.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.hospital.system.common.PageResult;
import com.cloud.hospital.system.dto.AddPatientDTO;
import com.cloud.hospital.system.dto.PatientQueryDTO;
import com.cloud.hospital.system.dto.UpdatePatientDTO;
import com.cloud.hospital.system.entity.Patient;
import com.cloud.hospital.system.entity.RegistrationOrder;
import com.cloud.hospital.system.mapper.PatientMapper;
import com.cloud.hospital.system.service.IPatientService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.hospital.system.service.IRegistrationOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 患者基础信息表 服务实现类
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements IPatientService {

    @Autowired
    private IRegistrationOrderService registrationOrderService;

    @Override
    public List<Patient> listPatients() {
        return this.list(new LambdaQueryWrapper<Patient>().orderByAsc(Patient::getId));
    }

    @Override
    public Patient getPatient(Long id) {
        Patient patient = this.getById(id);
        if (patient == null) {
            throw new RuntimeException("患者不存在");
        }
        return patient;
    }

    @Override
    public void addPatient(AddPatientDTO dto) {
        if (dto == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("患者姓名不能为空");
        }
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        if (dto.getIdCardNo() == null || dto.getIdCardNo().trim().isEmpty()) {
            throw new RuntimeException("身份证号不能为空");
        }

        long phoneCount = this.count(new LambdaQueryWrapper<Patient>().eq(Patient::getPhone, dto.getPhone().trim()));
        if (phoneCount > 0) {
            throw new RuntimeException("手机号已存在");
        }
        long idCardCount = this.count(new LambdaQueryWrapper<Patient>().eq(Patient::getIdCardNo, dto.getIdCardNo().trim()));
        if (idCardCount > 0) {
            throw new RuntimeException("身份证号已存在");
        }

        Patient patient = new Patient();
        patient.setName(dto.getName().trim());
        patient.setPhone(dto.getPhone().trim());
        patient.setIdCardNo(dto.getIdCardNo().trim());
        patient.setGender(dto.getGender());
        this.save(patient);
    }

    @Override
    public void updatePatient(UpdatePatientDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new RuntimeException("患者ID不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("患者姓名不能为空");
        }
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }
        if (dto.getIdCardNo() == null || dto.getIdCardNo().trim().isEmpty()) {
            throw new RuntimeException("身份证号不能为空");
        }

        Patient patient = this.getById(dto.getId());
        if (patient == null) {
            throw new RuntimeException("患者不存在");
        }

        long phoneCount = this.count(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getPhone, dto.getPhone().trim())
                .ne(Patient::getId, dto.getId()));
        if (phoneCount > 0) {
            throw new RuntimeException("手机号已存在");
        }
        long idCardCount = this.count(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getIdCardNo, dto.getIdCardNo().trim())
                .ne(Patient::getId, dto.getId()));
        if (idCardCount > 0) {
            throw new RuntimeException("身份证号已存在");
        }

        patient.setName(dto.getName().trim());
        patient.setPhone(dto.getPhone().trim());
        patient.setIdCardNo(dto.getIdCardNo().trim());
        patient.setGender(dto.getGender());
        this.updateById(patient);
    }

    @Override
    public void deletePatient(Long id) {
        Patient patient = this.getById(id);
        if (patient == null) {
            throw new RuntimeException("患者不存在");
        }

        long orderCount = registrationOrderService.count(new LambdaQueryWrapper<RegistrationOrder>().eq(RegistrationOrder::getPatientId, id));
        if (orderCount > 0) {
            throw new RuntimeException("该患者已有挂号订单，无法删除");
        }

        this.removeById(id);
    }

    @Override
    public PageResult<Patient> pageQuery(PatientQueryDTO queryDTO) {
        Page<Patient> pageParam = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getName() != null && !queryDTO.getName().trim().isEmpty()) {
            wrapper.like(Patient::getName, queryDTO.getName().trim());
        }
        wrapper.eq(queryDTO.getPhone() != null && !queryDTO.getPhone().trim().isEmpty(), Patient::getPhone, queryDTO.getPhone().trim());
        wrapper.eq(queryDTO.getIdCardNo() != null && !queryDTO.getIdCardNo().trim().isEmpty(), Patient::getIdCardNo, queryDTO.getIdCardNo().trim());
        wrapper.orderByAsc(Patient::getId);

        Page<Patient> rawPage = this.page(pageParam, wrapper);
        return new PageResult<>(rawPage.getTotal(), rawPage.getPages(), rawPage.getRecords());
    }

    @Override
    public void restorePatient(Long id) {
        if (id == null) {
            throw new RuntimeException("患者ID不能为空");
        }
        int affected = this.baseMapper.restoreById(id);
        if (affected == 0) {
            throw new RuntimeException("患者不存在或未被删除");
        }
    }
}
