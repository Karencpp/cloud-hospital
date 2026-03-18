package com.cloud.hospital.system.controller;

import com.cloud.hospital.system.common.Result;

import com.cloud.hospital.system.dto.BookOrderDTO;
import com.cloud.hospital.system.service.IRegistrationOrderService;
import com.cloud.hospital.system.vo.OrderResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/order")
@Tag(name = "1. 挂号订单核心交易模块", description = "处理高并发下的抢号下单业务")
public class RegistrationOrderController {
    @Autowired
    private RabbitTemplate rabbitTemplate;
  @Autowired
  private IRegistrationOrderService registrationOrderService;


    @PostMapping("/book")
    @Operation(summary = "接口1：提交抢号订单 (核心高并发)", description = "接受前端的高并发下单请求，进行库存扣减并生成订单")
    public Result<OrderResultVO> bookOrder(@RequestBody BookOrderDTO bookOrderDTO) {


        OrderResultVO vo = registrationOrderService.bookOrder(bookOrderDTO);


        return Result.success(vo);
    }
}