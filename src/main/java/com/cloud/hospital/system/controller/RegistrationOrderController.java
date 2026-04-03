package com.cloud.hospital.system.controller;

import com.cloud.hospital.system.common.Result;

import com.cloud.hospital.system.dto.BookOrderDTO;
import com.cloud.hospital.system.service.IRegistrationOrderService;
import com.cloud.hospital.system.vo.OrderAsyncResultVO;
import com.cloud.hospital.system.vo.OrderResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
@Tag(name = "1. 挂号订单核心交易模块", description = "处理高并发下的抢号下单业务")
public class RegistrationOrderController {
  @Autowired
  private IRegistrationOrderService registrationOrderService;


    @PostMapping("/book")
    @Operation(summary = "接口1：提交抢号订单 (核心高并发)", description = "接受前端的高并发下单请求，patientId 从登录态解析，仅需传 scheduleId")
    public Result<OrderResultVO> bookOrder(@RequestBody BookOrderDTO bookOrderDTO) {


        OrderResultVO vo = registrationOrderService.bookOrder(bookOrderDTO);

        return Result.success(vo);
    }

    @PostMapping("/cancel/{id}")
    @Operation(summary = "接口2：取消订单（主动取消）", description = "仅允许取消待支付订单，取消成功后释放号源并同步 Redis 库存")
    public Result<Void> cancelOrder(@PathVariable("id") Long orderId) {
        registrationOrderService.cancelOrder(orderId);
        return Result.success(null);
    }

    @PostMapping("/pay/{id}")
    @Operation(summary = "接口3：支付订单（模拟支付回调）", description = "仅允许将待支付订单更新为已支付，防止 MQ 超时取消误操作")
    public Result<Void> payOrder(@PathVariable("id") Long orderId) {
        registrationOrderService.payOrder(orderId);
        return Result.success(null);
    }

    @GetMapping("/result/{orderNo}")
    @Operation(summary = "接口4：查询异步下单结果", description = "用于异步下单场景查询排队中/成功/失败结果")
    public Result<OrderAsyncResultVO> queryOrderResult(@PathVariable String orderNo) {
        return Result.success(registrationOrderService.queryOrderResult(orderNo));
    }
}
