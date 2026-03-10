package com.cloud.hospital.system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 1. 标明这是一个 Spring Boot 启动类
@SpringBootApplication
// 2. 极其重要！告诉 MyBatis-Plus 你的 Mapper 接口都在哪个包下
// 面试踩坑点：如果不加这个，Spring Boot 启动时会报 "找不到 Mapper Bean" 的致命错误
@MapperScan("com.cloud.hospital.system.mapper") 
public class CloudHospitalApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudHospitalApplication.class, args);
        
        // 搞点仪式感，大厂的脚手架启动成功后通常都会打印专属的 Banner 或提示语
        System.out.println("==================================================");
        System.out.println("🚀🚀🚀 云医院后端系统 (Spring Boot 3.x) 启动成功! 🚀🚀🚀");
        System.out.println("==================================================");
    }
}