package com.cloud.hospital.system.mapper;

import com.cloud.hospital.system.entity.RegistrationOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 挂号订单表 Mapper 接口
 * </p>
 *
 * @author Allen
 * @since 2026-03-10
 */
@Mapper
public interface RegistrationOrderMapper extends BaseMapper<RegistrationOrder> {
    @Update("UPDATE `registration_order` SET status = 2 WHERE id = #{id} AND status = 0")
    int cancelOrderIfUnpaid(Long orderId);
}
