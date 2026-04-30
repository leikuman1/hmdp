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
    Result createVoucherOrder1(Long userId);

    /**
     * 创建订单
     * @param voucherOrder
     * @return
     */
    void createVoucherOrder(VoucherOrder voucherOrder);

    /**
     * 死信补偿使用：尽量确保订单创建成功
     * @param voucherOrder 订单
     * @return true 表示订单已存在或本次创建成功；false 表示创建失败
     */
    boolean tryCreateVoucherOrder(VoucherOrder voucherOrder);

    /**
     * 判断用户是否已经下过该券订单
     * @param userId 用户id
     * @param voucherId 优惠券id
     * @return true 表示订单已存在
     */
    boolean hasVoucherOrder(Long userId, Long voucherId);

    /**
     * 回滚 Redis 侧的库存与下单资格预占
     * @param voucherId 优惠券id
     * @param userId 用户id
     * @return true 表示回滚成功
     */
    boolean rollbackSeckillReservation(Long voucherId, Long userId);
}
