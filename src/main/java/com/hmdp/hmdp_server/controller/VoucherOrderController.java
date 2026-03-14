package com.hmdp.hmdp_server.controller;


import com.hmdp.hmdp_pojo.dto.Result;
import com.hmdp.hmdp_pojo.entity.VoucherOrder;
import com.hmdp.hmdp_server.service.IVoucherOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Api(tags = "优惠券订单管理")
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {
    @Resource
    private IVoucherOrderService iVoucherOrderService;

    @ApiOperation("秒杀优惠券")
    @PostMapping("seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return iVoucherOrderService.seckillVoucher(voucherId);
    }
}
