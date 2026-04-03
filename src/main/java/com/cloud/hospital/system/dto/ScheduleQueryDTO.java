package com.cloud.hospital.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "排班分页查询-请求参数")
public class ScheduleQueryDTO {

    @Schema(description = "医生ID（可选）", example = "1")
    private Long doctorId;

    @Schema(description = "科室ID（可选）", example = "1")
    private Long departmentId;

    @Schema(description = "查询起始日期（可选）", example = "2026-04-01")
    private LocalDate startDate;

    @Schema(description = "是否只查询有余号的排班", example = "true")
    private Boolean onlyAvailable;

    @Schema(description = "当前页码（从1开始）", example = "1")
    private int pageNum = 1;

    @Schema(description = "每页条数", example = "10")
    private int pageSize = 10;
}
