package com.cloud.hospital.system.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;

/**
 * 全局 Web 配置：一劳永逸解决日期转换问题
 *
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
    private AuthTokenInterceptor authTokenInterceptor;

    // 1. 针对 GET 请求的 @RequestParam 和 @PathVariable (Query 参数)
    @Override
    public void addFormatters(FormatterRegistry registry) {
        log.info("全局日期格式化配置开始...");
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(DateTimeFormatter.ofPattern(DATE_FORMAT));
        registrar.registerFormatters(registry);

    }

    // 2. 针对 POST 请求的 @RequestBody (JSON 数据)
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        log.info("全局日期格式化配置开始...");
        return builder -> {
            // 反序列化：前端传字符串 -> 后端 LocalDate 对象
            builder.deserializerByType(java.time.LocalDate.class, 
                    new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
            // 序列化：后端 LocalDate 对象 -> 前端字符串
            builder.serializerByType(java.time.LocalDate.class, 
                    new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authTokenInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns(
                        "/api/v1/auth/send-code",
                        "/api/v1/auth/register",
                        "/api/v1/auth/login",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/doc.html",
                        "/webjars/**",
                        "/favicon.ico",
                        "/error"
                );
    }
}
