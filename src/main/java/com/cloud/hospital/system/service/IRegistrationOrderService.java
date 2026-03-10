package com.cloud.hospital.system.service;

import com.cloud.hospital.system.dto.BookOrderDTO;
import com.cloud.hospital.system.entity.RegistrationOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.hospital.system.vo.OrderResultVO;

/**
 * <p>
 * 挂号订单表 服务类
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
public interface IRegistrationOrderService extends IService<RegistrationOrder> {

    OrderResultVO bookOrder(BookOrderDTO bookOrderDTO);
}
