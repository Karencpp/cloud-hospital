package com.cloud.hospital.system.service.impl;

import com.cloud.hospital.system.entity.Patient;
import com.cloud.hospital.system.mapper.PatientMapper;
import com.cloud.hospital.system.service.IPatientService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
