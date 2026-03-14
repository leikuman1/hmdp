package com.hmdp.hmdp_server.service;

import com.hmdp.hmdp_pojo.dto.Result;
import com.hmdp.hmdp_pojo.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    /**
     * 秒杀优惠券订单
     * @param voucherId
     * @return
     */
    Result seckillVoucher(Long voucherId);

    /**
     * 每个用户只能有一个订单
     * @param userId
     * @return
     */
    Result createVoucherOrder(Long userId);
}
