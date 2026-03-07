package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.SystemConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

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
        //构造商铺的key
        String key = RedisConstants.CACHE_SHOP_KEY + id;
        //查询缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //判断缓存有没有查到
        if (StrUtil.isNotBlank(shopJson)) {
            //存在就直接返回
            return Result.ok(JSONUtil.toBean(shopJson, Shop.class));
        }

        //没查到就直接查询数据库
        Shop shop = getById(id);
        //如果数据库也没查到，将空值放进缓存
        if (shop == null) {
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
            return Result.fail("the shop not exist");
        }
        //否则把结果写入缓存并设置超时时间
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);

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
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + id);
        return null;
    }

    @Override
    public Shop queryWithMutex(Long id) {
        //构造商铺的key
        String key = RedisConstants.CACHE_SHOP_KEY + id;
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
}
