package com.cloud.hospital.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "挂号订单创建结果-响应数据")
public class OrderResultVO {

    @Schema(description = "业务单号(雪花算法生成)", example = "ORD20260310223015")
    private String orderNo;

    @Schema(description = "需要支付的金额", example = "50.00")
    private BigDecimal payAmount;

    @Schema(description = "订单状态: 0-待支付", example = "0")
    private Integer status;
}