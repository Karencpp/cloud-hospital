package com.cloud.hospital.system.controller;

import com.cloud.hospital.system.common.Result;
import com.cloud.hospital.system.dto.AddDoctorDTO;
import com.cloud.hospital.system.dto.DoctorQueryDTO;
import com.cloud.hospital.system.dto.UpdateDoctorDTO;
import com.cloud.hospital.system.entity.Doctor;
import com.cloud.hospital.system.service.IDoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 医生信息表 前端控制器
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Slf4j
@RestController
@RequestMapping({"/system/doctor", "/api/v1/doctor"})
@Tag(name = "3. 医生管理模块", description = "负责医生的新增、编辑、删除与查询")
public class DoctorController {

    @Autowired
    private IDoctorService doctorService;

    @GetMapping("/list")
    @Operation(summary = "查询医生列表", description = "用于排班选择医生，支持按科室过滤")
    public Result<Object> listDoctors(@RequestParam(required = false) Long departmentId) {
        List<Doctor> list = doctorService.listDoctors(departmentId);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询医生详情", description = "根据医生ID查询医生信息")
    public Result<Object> getDoctor(@PathVariable Long id) {
        return Result.success(doctorService.getDoctor(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增医生", description = "新增医生信息，所属科室必须存在")
    public Result<Void> addDoctor(@RequestBody AddDoctorDTO dto) {
        doctorService.addDoctor(dto);
        return Result.success(null);
    }

    @PostMapping("/update")
    @Operation(summary = "修改医生", description = "修改医生基本信息，所属科室必须存在")
    public Result<Void> updateDoctor(@RequestBody UpdateDoctorDTO dto) {
        doctorService.updateDoctor(dto);
        return Result.success(null);
    }

    @PostMapping("/delete/{id}")
    @Operation(summary = "删除医生", description = "软删除医生（is_deleted=1），若存在未来排班则禁止删除")
    public Result<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return Result.success(null);
    }

    @PostMapping("/restore/{id}")
    @Operation(summary = "撤销删除医生", description = "将is_deleted字段恢复为 0")
    public Result<Void> restoreDoctor(@PathVariable Long id) {
        doctorService.restoreDoctor(id);
        return Result.success(null);
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询医生", description = "用于后台管理列表页，支持按科室/姓名/职称过滤")
    public Result<Object> pageQuery(@RequestBody DoctorQueryDTO queryDTO) {
        return Result.success(doctorService.pageQuery(queryDTO));
    }
}
