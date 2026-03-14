package com.hmdp.hmdp_server.service.impl;

import com.hmdp.hmdp_pojo.entity.BlogComments;
import com.hmdp.hmdp_server.mapper.BlogCommentsMapper;
import com.hmdp.hmdp_server.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
