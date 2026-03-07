package com.hmdp.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 查询各个类型的商铺
     * @return
     */
    @Override
    public Result queryShopTypeList() {
        String key= RedisConstants.SHOP_TYPE_KEY;
        //先查缓存
        String shopTypeList = stringRedisTemplate.opsForValue().get(key);
        //判断缓存里有没有查到
        if (StrUtil.isNotBlank(shopTypeList)) {
            //查到了就返回
            return Result.ok(JSONUtil.toBean(shopTypeList, new TypeReference<List<ShopType>>() {},true));
        }
        //没查到就再查数据库
        List<ShopType> shopList = query().orderByAsc("sort").list();
        //检查是否查到了
        if (CollUtil.isEmpty(shopList)) {
            //没查到就返回错误信息
            return Result.fail("query shopTypeList fail");
        }
        //查到了就写入缓存并设置过期时间
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shopList), RedisConstants.CACHE_SHOP_TTL, TimeUnit.MINUTES);
        //返回结果
        return null;
    }
}
