package com.hmdp.hmdp_server.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hmdp.hmdp_pojo.dto.Result;
import com.hmdp.hmdp_pojo.entity.Shop;
import com.hmdp.hmdp_server.mapper.ShopMapper;
import com.hmdp.hmdp_server.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.hmdp_common.utils.CacheClient;
import com.hmdp.hmdp_common.constant.RedisConstants;
import com.hmdp.hmdp_common.utils.RedisData;
import com.hmdp.hmdp_common.constant.SystemConstants;
import org.springframework.data.redis.ClusterRedirectException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.hmdp_common.constant.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
    /**
     * 根据id查询商铺信息
     * @param id
     * @return
     */
    @Override
    public Result queryById(Long id) {
        // 解决缓存穿透
        Shop shop = cacheClient
                .queryWithPassThrough(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 互斥锁解决缓存击穿
        // Shop shop = cacheClient
        //         .queryWithMutex(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);

        // 逻辑过期解决缓存击穿
        // Shop shop = cacheClient
        //         .queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, 20L, TimeUnit.SECONDS);

        if (shop == null) {
            return Result.fail("店铺不存在！");
        }
        // 7.返回
        return Result.ok(shop);
    }

    /**
     * 根据id更新商铺
     * @param shop
     * @return
     */
    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail(" the shop id can not be null");
        }
        //更新数据库
        updateById(shop);
        //删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return null;
    }

    @Override
    public Shop queryWithMutex(Long id) {
        //构造商铺的key
        String key = CACHE_SHOP_KEY + id;
        //查询缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //判断缓存有没有查到
        if (StrUtil.isNotBlank(shopJson)) {
            //存在就直接返回
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        //判断缓存里的是否为空值
        if (shopJson != null) {
            return null;
        }
        //没查到就尝试获取锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        Shop shop = null;
        try {
            boolean isLock = tryLock(lockKey);
            //如果失败就等待50ms重试
            if (!isLock) {
                Thread.sleep(50);
                return queryWithMutex(id);
            }
            //成功就查询数据库
            shop = getById(id);
            //如果数据库也没查到，返回错误
            if (shop == null) {
                //把空值写入数据库
                stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
                return null;
            }
            //否则把结果写入缓存并设置超时时间
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            unlock(lockKey);
        }
        return shop;
    }


    /**
     * 保存数据到redis
     * @param id
     * @param expireSeconds
     */
    public void saveShop2Redis(Long id, Long expireSeconds) {
        Shop shop = getById(id);
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, JSONUtil.toJsonStr(redisData));
    }

    public Shop queryWithLogicalExpire(Long id) {
        //先查缓存
        String key = CACHE_SHOP_KEY + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        //没查到就返回
        if (StrUtil.isBlank(json)) {
            return null;
        }
        //查到就先序列化
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        //判断是否过期
        if (expireTime != null && expireTime.isAfter(LocalDateTime.now())) {
            //没过期直接返回数据
            return shop;
        }
        //已过期就要重建缓存
        //先尝试拿锁
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(key);
        //判断拿锁是否成功
        if (isLock) {
            CACHE_REBUILD_EXECUTOR.submit(
                    ()->{
                        try {
                            this.saveShop2Redis(id, 20L);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            unlock(key);
                        }
                    }
            );
        }
        //无论是否拿到锁，都返回过期的商铺信息，保证系统可用性
        return shop;
    }

}
