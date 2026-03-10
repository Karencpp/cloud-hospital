package com.cloud.hospital.system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/ping")
    public String ping() {
        return "pong! 云医院系统全面就绪，准备迎接高并发抢号挑战！";
    }
}