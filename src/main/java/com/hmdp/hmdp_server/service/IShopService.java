package com.hmdp.hmdp_server.service;

import com.hmdp.hmdp_pojo.dto.Result;
import com.hmdp.hmdp_pojo.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {
    /**
     * 根据id查询商铺信息
     * @param id
     * @return
     */
    Result queryById(Long id);

    /**
     * 根据id更新商铺
     * @param shop
     * @return
     */
    Result update(Shop shop);

    /**
     * 根据互斥锁查询商铺
     * @param id
     * @return
     */
    Shop queryWithMutex(Long id);
}
