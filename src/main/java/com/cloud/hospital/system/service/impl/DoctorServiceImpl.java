package com.cloud.hospital.system.service.impl;

import com.cloud.hospital.system.entity.Doctor;
import com.cloud.hospital.system.mapper.DoctorMapper;
import com.cloud.hospital.system.service.IDoctorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
