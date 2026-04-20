package com.hmdp.hmdp_server.service.impl;

import com.hmdp.hmdp_common.config.RabbitMQConfig;
import com.hmdp.hmdp_common.constant.RedisConstants;
import com.hmdp.hmdp_common.utils.RedisRateLimiter;
import com.hmdp.hmdp_pojo.dto.Result;
import com.hmdp.hmdp_pojo.entity.VoucherOrder;
import com.hmdp.hmdp_server.mapper.VoucherOrderMapper;
import com.hmdp.hmdp_server.service.ISeckillVoucherService;
import com.hmdp.hmdp_server.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.hmdp_common.utils.RedisIdWorker;
import com.hmdp.hmdp_common.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    private static final long SECKILL_RATE_LIMIT_WINDOW_MILLIS = 1000L;
    private static final long SECKILL_RATE_LIMIT_MAX_REQUESTS = 1L;

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RedisRateLimiter redisRateLimiter;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    /**
     * 秒杀优惠券订单
     * @param voucherId
     * @return
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        //获取用户
        Long userId = UserHolder.getUser().getId();
        String rateLimitKey = RedisConstants.SECKILL_RATE_LIMIT_KEY + voucherId + ":" + userId;
        boolean allowed = redisRateLimiter.isAllowed(
                rateLimitKey,
                SECKILL_RATE_LIMIT_WINDOW_MILLIS,
                SECKILL_RATE_LIMIT_MAX_REQUESTS
        );
        if (!allowed) {
            return Result.fail("请求过于频繁，请稍后重试");
        }

        long orderId = redisIdWorker.nextId("order");
        // 1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId)
        );
        int r = result.intValue();
        // 2.判断结果是否为0
        if (r != 0) {
            // 2.1.不为0 ，代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        voucherOrder.setVoucherId(voucherId);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SECKILL_EXCHANGE,
                RabbitMQConfig.SECKILL_ROUTING_KEY,
                voucherOrder
        );
        // 3.返回订单id
        return Result.ok(orderId);
    }

    @Override
    public Result createVoucherOrder1(Long userId) {
        return null;
    }

    /**
     *
     * @param voucherOrder
     * @return
     */
    @Transactional
    @Override
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();

        // 1) 幂等校验：同一用户同一券只能一单
        int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            log.warn("重复下单, userId={}, voucherId={}", userId, voucherId);
            return;
        }

        // 2) 扣库存（CAS风格）
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if (!success) {
            log.warn("库存不足, voucherId={}",voucherId);
            return;
        }

        // 3) 保存订单（建议数据库有唯一索引兜底）
        try {
            save(voucherOrder);
        } catch (DuplicateKeyException e) {
            // 并发极端场景下的最终幂等兜底
            log.warn("唯一索引拦截重复订单, userId={}, voucherId={}", userId, voucherId);
        }
    }


}
