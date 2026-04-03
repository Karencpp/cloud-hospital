package com.cloud.hospital.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cloud.hospital.system.constant.RedisKeyPrefix;
import com.cloud.hospital.system.common.PageResult;
import com.cloud.hospital.system.dto.AddDepartmentDTO;
import com.cloud.hospital.system.dto.DepartmentQueryDTO;
import com.cloud.hospital.system.dto.UpdateDepartmentDTO;
import com.cloud.hospital.system.entity.Department;
import com.cloud.hospital.system.entity.Doctor;
import com.cloud.hospital.system.mapper.DepartmentMapper;
import com.cloud.hospital.system.mapper.DoctorMapper;
import com.cloud.hospital.system.service.IDepartmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

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

    private static final Duration DEPARTMENT_CACHE_TTL = Duration.ofHours(6);

    @Autowired
    private DoctorMapper doctorMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<Department> listDepartments() {
        String cached = stringRedisTemplate.opsForValue().get(RedisKeyPrefix.DEPARTMENT_LIST_KEY);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<Department>>() {});
            } catch (Exception ignored) {
            }
        }

        List<Department> list = this.list(new LambdaQueryWrapper<Department>().orderByAsc(Department::getId));
        try {
            stringRedisTemplate.opsForValue().set(RedisKeyPrefix.DEPARTMENT_LIST_KEY, objectMapper.writeValueAsString(list), DEPARTMENT_CACHE_TTL);
        } catch (JsonProcessingException ignored) {
        }
        return list;
    }

    @Override
    public Department getDepartment(Long id) {
        if (id == null) {
            throw new RuntimeException("科室ID不能为空");
        }

        String redisKey = RedisKeyPrefix.DEPARTMENT_KEY_PREFIX + id;
        String cached = stringRedisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, Department.class);
            } catch (Exception ignored) {
            }
        }

        Department department = this.getById(id);
        if (department == null) {
            throw new RuntimeException("科室不存在");
        }

        try {
            stringRedisTemplate.opsForValue().set(redisKey, objectMapper.writeValueAsString(department), DEPARTMENT_CACHE_TTL);
        } catch (JsonProcessingException ignored) {
        }
        return department;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDepartment(AddDepartmentDTO dto) {
        if (dto == null) {
            throw new RuntimeException("请求参数不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("科室名称不能为空");
        }
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new RuntimeException("科室编码不能为空");
        }

        long codeCount = this.count(new LambdaQueryWrapper<Department>().eq(Department::getCode, dto.getCode()));
        if (codeCount > 0) {
            throw new RuntimeException("科室编码已存在");
        }

        Department department = new Department();
        department.setName(dto.getName().trim());
        department.setCode(dto.getCode().trim());
        department.setDescription(dto.getDescription());
        this.save(department);

        stringRedisTemplate.delete(RedisKeyPrefix.DEPARTMENT_LIST_KEY);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDepartment(UpdateDepartmentDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new RuntimeException("科室ID不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("科室名称不能为空");
        }
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new RuntimeException("科室编码不能为空");
        }

        Department department = this.getById(dto.getId());
        if (department == null) {
            throw new RuntimeException("科室不存在");
        }

        long codeCount = this.count(new LambdaQueryWrapper<Department>()
                .eq(Department::getCode, dto.getCode())
                .ne(Department::getId, dto.getId()));
        if (codeCount > 0) {
            throw new RuntimeException("科室编码已存在");
        }

        department.setName(dto.getName().trim());
        department.setCode(dto.getCode().trim());
        department.setDescription(dto.getDescription());
        this.updateById(department);

        stringRedisTemplate.delete(RedisKeyPrefix.DEPARTMENT_LIST_KEY);
        stringRedisTemplate.delete(RedisKeyPrefix.DEPARTMENT_KEY_PREFIX + dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long id) {
        Department department = this.getById(id);
        if (department == null) {
            throw new RuntimeException("科室不存在");
        }

        Long doctorCount = doctorMapper.selectCount(new LambdaQueryWrapper<Doctor>().eq(Doctor::getDepartmentId, id));
        if (doctorCount != null && doctorCount > 0) {
            throw new RuntimeException("该科室下仍存在医生，无法删除");
        }

        this.removeById(id);

        stringRedisTemplate.delete(RedisKeyPrefix.DEPARTMENT_LIST_KEY);
        stringRedisTemplate.delete(RedisKeyPrefix.DEPARTMENT_KEY_PREFIX + id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreDepartment(Long id) {
        if (id == null) {
            throw new RuntimeException("科室ID不能为空");
        }

        int affected = this.baseMapper.restoreById(id);
        if (affected == 0) {
            throw new RuntimeException("科室不存在或未被删除");
        }

        stringRedisTemplate.delete(RedisKeyPrefix.DEPARTMENT_LIST_KEY);
        stringRedisTemplate.delete(RedisKeyPrefix.DEPARTMENT_KEY_PREFIX + id);

        this.listDepartments();
    }

    @Override
    public PageResult<Department> pageQuery(DepartmentQueryDTO queryDTO) {
        Page<Department> pageParam = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getName() != null && !queryDTO.getName().trim().isEmpty()) {
            wrapper.like(Department::getName, queryDTO.getName().trim());
        }
        wrapper.eq(queryDTO.getCode() != null && !queryDTO.getCode().trim().isEmpty(), Department::getCode, queryDTO.getCode().trim());
        wrapper.orderByAsc(Department::getId);

        Page<Department> rawPage = this.page(pageParam, wrapper);
        return new PageResult<>(rawPage.getTotal(), rawPage.getPages(), rawPage.getRecords());
    }
}
