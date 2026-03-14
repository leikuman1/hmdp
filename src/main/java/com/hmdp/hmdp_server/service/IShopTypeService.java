package com.hmdp.hmdp_server.service;

import com.hmdp.hmdp_pojo.dto.Result;
import com.hmdp.hmdp_pojo.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopTypeService extends IService<ShopType> {

    /**
     * 查询各个类型的店铺
     * @return
     */
    Result queryShopTypeList();
}
