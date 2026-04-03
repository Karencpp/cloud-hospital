package com.cloud.hospital.system.controller;

import com.cloud.hospital.system.common.Result;
import com.cloud.hospital.system.dto.AddPatientDTO;
import com.cloud.hospital.system.dto.PatientQueryDTO;
import com.cloud.hospital.system.dto.UpdatePatientDTO;
import com.cloud.hospital.system.entity.Patient;
import com.cloud.hospital.system.service.IPatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 患者基础信息表 前端控制器
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Slf4j
@RestController
@RequestMapping({"/system/patient", "/api/v1/patient"})
@Tag(name = "4. 患者管理模块", description = "负责患者的新增、编辑、删除与查询")
public class PatientController {

    @Autowired
    private IPatientService patientService;

    @GetMapping("/list")
    @Operation(summary = "查询患者列表", description = "用于后台快速查看患者列表")
    public Result<Object> listPatients() {
        List<Patient> list = patientService.listPatients();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询患者详情", description = "根据患者ID查询患者信息")
    public Result<Object> getPatient(@PathVariable Long id) {
        return Result.success(patientService.getPatient(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增患者", description = "新增患者信息，手机号与身份证号需唯一")
    public Result<Void> addPatient(@RequestBody AddPatientDTO dto) {
        patientService.addPatient(dto);
        return Result.success(null);
    }

    @PostMapping("/update")
    @Operation(summary = "修改患者", description = "修改患者信息，手机号与身份证号需唯一")
    public Result<Void> updatePatient(@RequestBody UpdatePatientDTO dto) {
        patientService.updatePatient(dto);
        return Result.success(null);
    }

    @PostMapping("/delete/{id}")
    @Operation(summary = "删除患者", description = "软删除患者（is_deleted=1），若存在挂号订单则禁止删除")
    public Result<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return Result.success(null);
    }

    @PostMapping("/restore/{id}")
    @Operation(summary = "撤销删除患者", description = "将is_deleted字段恢复为 0")
    public Result<Void> restorePatient(@PathVariable Long id) {
        patientService.restorePatient(id);
        return Result.success(null);
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询患者", description = "用于后台管理列表页，支持按姓名/手机号/身份证号过滤")
    public Result<Object> pageQuery(@RequestBody PatientQueryDTO queryDTO) {
        return Result.success(patientService.pageQuery(queryDTO));
    }
}
