# 黑马点评 (hm-dianping)

一个基于 Spring Boot 的点评服务系统，提供商铺管理、用户登录、优惠券、博客等核心功能。

## 技术栈

- **后端框架**: Spring Boot 2.3.12
- **ORM**: MyBatis-Plus 3.4.3
- **缓存**: Redis (Lettuce)
- **数据库**: MySQL 5.7+
- **接口文档**: Knife4j (Swagger2)
- **工具库**: Hutool 5.7.17
- **Java 版本**: 1.8

## 项目结构

```
src/main/java/com/hmdp/
├── HmDianPingApplication.java          # 启动类
├── hmdp_common/                        # 公共模块
│   ├── config/                         # 配置类
│   │   ├── Knife4jConfig.java          # Knife4j配置
│   │   ├── MvcConfig.java              # MVC配置(拦截器)
│   │   ├── MybatisConfig.java          # MyBatis配置
│   │   └── WebExceptionAdvice.java     # 全局异常处理
│   ├── constant/                       # 常量类
│   │   ├── RedisConstants.java         # Redis常量
│   │   └── SystemConstants.java        # 系统常量
│   └── utils/                          # 工具类
│       ├── CacheClient.java            # Redis缓存客户端
│       ├── LoginInterceptor.java       # 登录拦截器
│       ├── PasswordEncoder.java        # 密码加密
│       ├── RedisData.java              # Redis数据结构
│       ├── RedisIdWorker.java          # 分布式ID生成器
│       ├── RefreshTokenInterceptor.java # Token刷新拦截器
│       ├── RegexPatterns.java          # 正则表达式
│       ├── RegexUtils.java             # 正则工具
│       └── UserHolder.java             # 用户上下文
├── hmdp_pojo/                          # 数据对象模块
│   ├── dto/                            # 数据传输对象
│   │   ├── LoginFormDTO.java           # 登录表单
│   │   ├── Result.java                 # 统一响应
│   │   ├── ScrollResult.java           # 滚动分页结果
│   │   └── UserDTO.java                # 用户DTO
│   └── entity/                         # 实体类
│       ├── Blog.java                   # 博客
│       ├── BlogComments.java           # 博客评论
│       ├── Follow.java                 # 关注
│       ├── SeckillVoucher.java         # 秒杀券
│       ├── Shop.java                   # 商铺
│       ├── ShopType.java               # 商铺类型
│       ├── User.java                   # 用户
│       ├── UserInfo.java               # 用户信息
│       ├── Voucher.java                # 优惠券
│       └── VoucherOrder.java           # 优惠券订单
└── hmdp_server/                        # 业务模块
    ├── controller/                     # 控制器
    │   ├── BlogCommentsController.java # 博客评论
    │   ├── BlogController.java         # 博客
    │   ├── FollowController.java       # 关注
    │   ├── ShopController.java         # 商铺
    │   ├── ShopTypeController.java     # 商铺类型
    │   ├── UploadController.java       # 文件上传
    │   ├── UserController.java         # 用户
    │   ├── VoucherController.java      # 优惠券
    │   └── VoucherOrderController.java # 优惠券订单
    ├── mapper/                         # MyBatis映射器
    └── service/                        # 业务服务
        └── impl/                       # 服务实现
```

## 功能列表

### 用户模块
- [x] 手机号验证码登录
- [x] 账号密码登录
- [x] 获取当前登录用户
- [x] 用户登出

### 商铺模块
- [x] 根据ID查询商铺
- [x] 新增商铺
- [x] 更新商铺
- [x] 按类型分页查询商铺
- [x] 按名称关键字分页查询商铺

### 商铺类型模块
- [x] 查询所有商铺类型

### 博客模块
- [x] 发布博客
- [x] 点赞博客
- [x] 查询我的博客
- [x] 查询热门博客

### 优惠券模块
- [x] 新增普通券
- [x] 新增秒杀券
- [x] 查询店铺优惠券列表
- [x] 秒杀优惠券（一人一单、乐观锁防超卖）

### 文件上传模块
- [x] 上传博客图片
- [x] 删除博客图片

### 博客评论模块
- [ ] 暂无实现

### 关注模块
- [ ] 暂无实现

## 核心功能说明

### 缓存策略
- **缓存穿透**: 存入空值解决
- **缓存击穿**: 互斥锁 + 逻辑过期解决
- **缓存雪崩**: 随机TTL解决

### 分布式锁
- 基于Redis实现分布式锁
- 解决一人一单并发问题

### 分布式ID
- 基于Redis自增生成唯一ID
- 包含时间戳和序列号

## 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- Redis 3.2+
- Nginx (可选，用于负载均衡)

### 配置数据库和Redis

修改 `src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/hmdp?useSSL=false&serverTimezone=UTC
    username: root
    password: 123456
  redis:
    host: 192.168.186.128
    port: 6379
    password: 123321
```

### 初始化数据库

执行 `src/main/resources/db/hmdp.sql` 初始化数据库表。

### Nginx 负载均衡配置（可选）

项目提供了 Nginx 配置文件，可实现对 8081 和 8082 两个服务实例的轮询负载均衡。

配置文件位于 `nginx/` 目录下：

```
nginx/
├── nginx.conf          # Nginx主配置文件
└── conf.d/
    └── hmdp.conf       # 黑马点评服务配置
```

#### 使用方式

1. 将 `nginx/nginx.conf` 替换 Nginx 安装目录下的 `conf/nginx.conf`
2. 将 `nginx/conf.d/hmdp.conf` 放到 Nginx 安装目录的 `conf/conf.d/` 目录下
3. 启动两个 Spring Boot 实例（分别使用 8081 和 8082 端口）
4. 启动 Nginx

```bash
# 启动 Nginx
nginx

# 重载配置
nginx -s reload

# 停止 Nginx
nginx -s stop
```

#### 配置说明

- Nginx 监听端口：8080
- 后端服务1：127.0.0.1:8081
- 后端服务2：127.0.0.1:8082
- 轮询策略：weight=1（权重相同）
- 故障转移：max_fails=5, fail_timeout=10s

### 编译运行

```bash
# 编译项目
mvn clean package -DskipTests

# 运行项目
mvn spring-boot:run

# 或者直接运行jar
java -jar target/hm-dianping-0.0.1-SNAPSHOT.jar
```

### 访问接口文档

- 直接访问： http://localhost:8081/doc.html
- 通过 Nginx 访问： http://localhost:8080/doc.html

## 接口文档

启动项目后，通过 Knife4j 生成的可视化接口文档进行测试。

主要接口：

| 模块 | 接口路径 | 说明 |
|------|----------|------|
| 用户 | POST /user/code | 发送验证码 |
| 用户 | POST /user/login | 用户登录 |
| 用户 | GET /user/me | 获取当前用户 |
| 商铺 | GET /shop/{id} | 查询商铺 |
| 商铺 | GET /shop/of/type | 按类型查询 |
| 商铺类型 | GET /shop-type/list | 查询类型列表 |
| 博客 | POST /blog | 发布博客 |
| 博客 | GET /blog/hot | 热门博客 |
| 优惠券 | POST /voucher | 新增优惠券 |
| 优惠券 | POST /voucher/seckill | 新增秒杀券 |
| 优惠券 | GET /voucher/list/{shopId} | 查询优惠券 |
| 优惠券订单 | POST /voucher-order/seckill/{id} | 秒杀下单 |
| 文件上传 | POST /upload/blog | 上传图片 |

## 更新日志

### v1.0.2 (2026-03-15)
- 添加 Nginx 负载均衡配置，支持轮询 8081 和 8082 两个服务实例

### v1.0.1 (2026-03-15)
- 添加 Knife4j 接口文档
- 排除文档路径的拦截

### v1.0.0 (2026-03-13)
- 重构项目结构，分为 hmdp_common、hmdp_pojo、hmdp_server 三个模块
- 实现商铺管理功能（含缓存）
- 实现用户登录功能
- 实现优惠券秒杀功能（一人一单、乐观锁）
- 实现分布式ID生成器
- 实现文件上传功能

## 许可证

MIT License