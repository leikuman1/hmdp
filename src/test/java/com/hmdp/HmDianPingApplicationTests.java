package com.hmdp;


import com.hmdp.hmdp_common.config.RedissonConfig;
import com.hmdp.hmdp_common.utils.CacheClient;
import com.hmdp.hmdp_common.utils.RedisIdWorker;
import com.hmdp.hmdp_pojo.entity.SeckillVoucher;
import com.hmdp.hmdp_server.service.ISeckillVoucherService;
import com.hmdp.hmdp_server.service.IShopService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private RedissonClient redissonClient;

    private ExecutorService es = Executors.newFixedThreadPool(500);

//    @Test
//    void testIdWorker() throws InterruptedException {
//        CountDownLatch latch = new CountDownLatch(300);
//
//        Runnable task = () -> {
//            for (int i = 0; i < 100; i++) {
//                long id = redisIdWorker.nextId("order");
//                System.out.println("id = " + id);
//            }
//            latch.countDown();
//        };
//        long begin = System.currentTimeMillis();
//        for (int i = 0; i < 300; i++) {
//            es.submit(task);
//        }
//        latch.await();
//        long end = System.currentTimeMillis();
//        System.out.println("time = " + (end - begin));
//    }
//
//    @Test
//    void testTime() {
//        System.out.println(LocalDateTime.now());
//        SeckillVoucher voucher = voucherService.getById(10L);
//        System.out.println(voucher.getEndTime());
//        System.out.println( java.time.ZoneId.systemDefault());
//    }
//
//    @Test
//    void testRedisson() throws Exception{
//        //获取锁(可重入)，指定锁的名称
//        RLock lock = redissonClient.getLock("anyLock");
//        //尝试获取锁，参数分别是：获取锁的最大等待时间(期间会重试)，锁自动释放时间，时间单位
//        boolean isLock = lock.tryLock(1,10, TimeUnit.SECONDS);
//        //判断获取锁成功
//        if(isLock){
//            try{
//                System.out.println("执行业务");
//            }finally{
//                //释放锁
//                lock.unlock();
//            }
//
//        }
//    }
}
