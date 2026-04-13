package com.hmdp.hmdp_common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.beans.factory.annotation.Value;


@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + redisHost + ":" + redisPort;
        if (redisPassword == null || redisPassword.isEmpty()) {
            config.useSingleServer().setAddress(address);
        } else {
            config.useSingleServer().setAddress(address).setPassword(redisPassword);
        }
        return Redisson.create(config);
    }
}
