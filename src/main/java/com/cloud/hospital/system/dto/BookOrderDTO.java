package com.cloud.hospital.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "提交挂号预约订单-请求参数")
public class BookOrderDTO {

    @Schema(description = "要抢的排班ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1001")
    private Long scheduleId;

    @Schema(description = "当前就诊患者ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "999")
    private Long patientId;
}