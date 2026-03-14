package com.hmdp.hmdp_server.controller;


import com.hmdp.hmdp_pojo.dto.LoginFormDTO;
import com.hmdp.hmdp_pojo.dto.Result;
import com.hmdp.hmdp_pojo.dto.UserDTO;
import com.hmdp.hmdp_pojo.entity.UserInfo;
import com.hmdp.hmdp_server.service.IUserInfoService;
import com.hmdp.hmdp_server.service.IUserService;
import com.hmdp.hmdp_common.utils.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Api(tags = "用户管理")
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 发送手机验证码
     */
    @ApiOperation("发送手机验证码")
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // TODO 发送短信验证码并保存验证码
        return userService.sendCode(phone, session);
    }

    /**
     * 登录功能
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        //实现登录功能
        return userService.login(loginForm, session);
    }

    /**
     * 登出功能
     * @return 无
     */
    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result logout(){
        // TODO 实现登出功能
        return Result.fail("功能未完成");
    }

    @ApiOperation("获取当前登录用户信息")
    @GetMapping("/me")
    public Result me(){
        //取当前登录的用户并返回
        UserDTO userDTO = UserHolder.getUser();
        return Result.ok(userDTO);
    }

    @ApiOperation("根据id查询用户详情")
    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }
}
