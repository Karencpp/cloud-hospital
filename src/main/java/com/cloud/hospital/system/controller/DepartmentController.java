package com.cloud.hospital.system.controller;

import com.cloud.hospital.system.common.Result;
import com.cloud.hospital.system.dto.AddDepartmentDTO;
import com.cloud.hospital.system.dto.DepartmentQueryDTO;
import com.cloud.hospital.system.dto.UpdateDepartmentDTO;
import com.cloud.hospital.system.entity.Department;
import com.cloud.hospital.system.service.IDepartmentService;
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
 * 医院科室表 前端控制器
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Slf4j
@RestController
@RequestMapping({"/system/department", "/api/v1/department"})
@Tag(name = "1. 科室管理模块", description = "负责科室的新增、编辑、删除与查询")
public class DepartmentController {

    @Autowired
    private IDepartmentService departmentService;

    @GetMapping("/list")
    @Operation(summary = "查询科室列表", description = "用于前端下拉框或导航展示")
    public Result<Object> listDepartments() {
        List<Department> list = departmentService.listDepartments();
        return Result.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询科室详情", description = "根据科室ID查询科室信息")
    public Result<Object> getDepartment(@PathVariable Long id) {
        return Result.success(departmentService.getDepartment(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增科室", description = "创建新的科室信息，科室编码需唯一")
    public Result<Void> addDepartment(@RequestBody AddDepartmentDTO dto) {
        departmentService.addDepartment(dto);
        return Result.success(null);
    }

    @PostMapping("/update")
    @Operation(summary = "修改科室", description = "更新科室名称、编码与描述，科室编码需唯一")
    public Result<Void> updateDepartment(@RequestBody UpdateDepartmentDTO dto) {
        departmentService.updateDepartment(dto);
        return Result.success(null);
    }

    @PostMapping("/delete/{id}")
    @Operation(summary = "删除科室", description = "软删除科室（is_deleted=1），若科室下仍存在医生则禁止删除")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return Result.success(null);
    }

    @PostMapping("/restore/{id}")
    @Operation(summary = "撤销删除科室", description = "将is_deleted字段恢复为 0，并刷新科室列表缓存")
    public Result<Void> restoreDepartment(@PathVariable Long id) {
        departmentService.restoreDepartment(id);
        return Result.success(null);
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询科室", description = "用于后台管理列表页，支持按名称/编码过滤")
    public Result<Object> pageQuery(@RequestBody DepartmentQueryDTO queryDTO) {
        return Result.success(departmentService.pageQuery(queryDTO));
    }
}
