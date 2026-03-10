package com.cloud.hospital.system.OpenApiConfig;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局接口文档配置类 (Spring Boot 3 + OpenAPI 3 规范)
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cloudHospitalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("云医院核心交易中台 - API 文档")
                        .description("大厂高并发抢号架构实战项目。提供挂号预约、专家排班、库存扣减等核心业务接口。")
                        .version("v1.0.0")
                        // 换成你的名字，以后面试展示项目时这就是你的专属烙印
                        .contact(new Contact().name("Allen").email("2095651017@qq.com")));
    }
}