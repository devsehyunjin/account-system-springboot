package com.devsehyunjin.account.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.embedded.RedisServer;

@Configuration
public class RedisConfig {
    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        redisServer = new RedisServer(6379); // yml과 동일 포트
        redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) redisServer.stop();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }
}