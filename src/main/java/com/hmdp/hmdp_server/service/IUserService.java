package com.hmdp.hmdp_server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.hmdp_pojo.dto.LoginFormDTO;
import com.hmdp.hmdp_pojo.dto.Result;
import com.hmdp.hmdp_pojo.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    /**
     * 发送验证码
     * @param phone
     * @param session
     * @return
     */
    Result sendCode(String phone, HttpSession session);

    /**
     * 用户登录
     * @param loginForm
     * @param session
     * @return
     */
    Result login(LoginFormDTO loginForm, HttpSession session);
}
