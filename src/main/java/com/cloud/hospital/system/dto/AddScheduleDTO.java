package com.cloud.hospital.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "新增排班-请求参数")
public class AddScheduleDTO {

    @Schema(description = "医生ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long doctorId;

    @Schema(description = "科室ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long departmentId;

    @Schema(description = "出诊日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-04-10")
    private LocalDate workDate;

    @Schema(description = "班次：1-上午, 2-下午, 3-全天", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Byte shiftType;

    @Schema(description = "总号源数", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    private Integer totalNum;

    @Schema(description = "挂号费(保留两位小数)", requiredMode = Schema.RequiredMode.REQUIRED, example = "50.00")
    private BigDecimal amount;

    @Schema(description = "状态：0-停诊, 1-正常出诊", example = "1")
    private Byte status;
}
