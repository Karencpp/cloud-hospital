package com.cloud.hospital.system.service.impl;

import com.cloud.hospital.system.entity.Department;
import com.cloud.hospital.system.mapper.DepartmentMapper;
import com.cloud.hospital.system.service.IDepartmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 医院科室表 服务实现类
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements IDepartmentService {

}
