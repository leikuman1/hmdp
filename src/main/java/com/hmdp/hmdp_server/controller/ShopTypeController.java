package com.hmdp.hmdp_server.controller;


import com.hmdp.hmdp_pojo.dto.Result;
import com.hmdp.hmdp_pojo.entity.ShopType;
import com.hmdp.hmdp_server.service.IShopTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Api(tags = "商铺类型管理")
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @ApiOperation("查询商铺类型列表")
    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryShopTypeList();
    }
}
