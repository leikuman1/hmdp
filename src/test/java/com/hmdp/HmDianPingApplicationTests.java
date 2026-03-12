package com.hmdp;

import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.Voucher;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IShopService;
import com.hmdp.service.IVoucherService;
import com.hmdp.utils.CacheClient;
import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class HmDianPingApplicationTests {
    @Resource
    private CacheClient cacheClient;
    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private IShopService iShopService;

    @Resource
    private ISeckillVoucherService voucherService;


    private ExecutorService es = Executors.newFixedThreadPool(500);

    @Test
    void testIdWorker() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(300);

        Runnable task = () -> {
            for (int i = 0; i < 100; i++) {
                long id = redisIdWorker.nextId("order");
                System.out.println("id = " + id);
            }
            latch.countDown();
        };
        long begin = System.currentTimeMillis();
        for (int i = 0; i < 300; i++) {
            es.submit(task);
        }
        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("time = " + (end - begin));
    }

    @Test
    void testTime() {
        System.out.println(LocalDateTime.now());
        SeckillVoucher voucher = voucherService.getById(10L);
        System.out.println(voucher.getEndTime());
        System.out.println( java.time.ZoneId.systemDefault());
    }
}
