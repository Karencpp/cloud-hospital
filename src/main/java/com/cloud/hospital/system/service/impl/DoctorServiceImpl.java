package com.cloud.hospital.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.hospital.system.common.PageResult;
import com.cloud.hospital.system.dto.AddDoctorDTO;
import com.cloud.hospital.system.dto.DoctorQueryDTO;
import com.cloud.hospital.system.dto.UpdateDoctorDTO;
import com.cloud.hospital.system.entity.Department;
import com.cloud.hospital.system.entity.Doctor;
import com.cloud.hospital.system.entity.Schedule;
import com.cloud.hospital.system.mapper.DoctorMapper;
import com.cloud.hospital.system.service.IDepartmentService;
import com.cloud.hospital.system.service.IDoctorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.hospital.system.service.IScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 医生信息表 服务实现类
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements IDoctorService {

    @Autowired
    private IDepartmentService departmentService;

    @Autowired
    private IScheduleService scheduleService;

    @Override
    public List<Doctor> listDoctors(Long departmentId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(departmentId != null, Doctor::getDepartmentId, departmentId);
        wrapper.orderByAsc(Doctor::getId);
        return this.list(wrapper);
    }

    @Override
    public Doctor getDoctor(Long id) {
        Doctor doctor = this.getById(id);
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }
        return doctor;
    }

    @Override
    public void addDoctor(AddDoctorDTO dto) {
        if (dto == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("医生姓名不能为空");
        }
        if (dto.getDepartmentId() == null) {
            throw new RuntimeException("所属科室不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new RuntimeException("职称不能为空");
        }

        Department department = departmentService.getById(dto.getDepartmentId());
        if (department == null) {
            throw new RuntimeException("科室不存在");
        }

        Doctor doctor = new Doctor();
        doctor.setName(dto.getName().trim());
        doctor.setDepartmentId(dto.getDepartmentId());
        doctor.setTitle(dto.getTitle().trim());
        doctor.setSpecialty(dto.getSpecialty());
        this.save(doctor);
    }

    @Override
    public void updateDoctor(UpdateDoctorDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new RuntimeException("医生ID不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("医生姓名不能为空");
        }
        if (dto.getDepartmentId() == null) {
            throw new RuntimeException("所属科室不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new RuntimeException("职称不能为空");
        }

        Doctor doctor = this.getById(dto.getId());
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }

        Department department = departmentService.getById(dto.getDepartmentId());
        if (department == null) {
            throw new RuntimeException("科室不存在");
        }

        doctor.setName(dto.getName().trim());
        doctor.setDepartmentId(dto.getDepartmentId());
        doctor.setTitle(dto.getTitle().trim());
        doctor.setSpecialty(dto.getSpecialty());
        this.updateById(doctor);
    }

    @Override
    public void deleteDoctor(Long id) {
        Doctor doctor = this.getById(id);
        if (doctor == null) {
            throw new RuntimeException("医生不存在");
        }

        long scheduleCount = scheduleService.count(new LambdaQueryWrapper<Schedule>()
                .eq(Schedule::getDoctorId, id)
                .ge(Schedule::getWorkDate, LocalDate.now()));
        if (scheduleCount > 0) {
            throw new RuntimeException("该医生存在排班，无法删除");
        }

        this.removeById(id);
    }

    @Override
    public PageResult<Doctor> pageQuery(DoctorQueryDTO queryDTO) {
        Page<Doctor> pageParam = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getDepartmentId() != null, Doctor::getDepartmentId, queryDTO.getDepartmentId());
        if (queryDTO.getName() != null && !queryDTO.getName().trim().isEmpty()) {
            wrapper.like(Doctor::getName, queryDTO.getName().trim());
        }
        if (queryDTO.getTitle() != null && !queryDTO.getTitle().trim().isEmpty()) {
            wrapper.like(Doctor::getTitle, queryDTO.getTitle().trim());
        }
        wrapper.orderByAsc(Doctor::getId);

        Page<Doctor> rawPage = this.page(pageParam, wrapper);
        return new PageResult<>(rawPage.getTotal(), rawPage.getPages(), rawPage.getRecords());
    }

    @Override
    public void restoreDoctor(Long id) {
        if (id == null) {
            throw new RuntimeException("医生ID不能为空");
        }
        int affected = this.baseMapper.restoreById(id);
        if (affected == 0) {
            throw new RuntimeException("医生不存在或未被删除");
        }
    }
}
