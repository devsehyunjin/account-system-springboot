package com.devsehyunjin.account.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import redis.embedded.RedisServer;

@Slf4j
@Configuration
public class RedisConfig {
    private RedisServer redisServer;

    @Value("${spring.data.redis.port}")
    private int redisPort;

//    @PostConstruct
//    public void startEmbeddedRedis() {
//        try {
//            // Embedded Redis 서버 시작
//            log.info("Embedded Redis 시작");
//            redisServer = RedisServer.builder()
//                    .port(redisPort)
//                    .setting("maxmemory 128M")
//                    .build();
//            redisServer.start();
//        } catch (Exception e) {
//            System.err.println("Embedded Redis 실행 중 오류 발생: " + e.getMessage());
//        }
//    }

    @PreDestroy
    public void stopEmbeddedRedis() {
        // 애플리케이션 종료 시 Embedded Redis 중지
        if (redisServer != null) {
            redisServer.stop();
        }
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.data.redis.host}") String redisHost,
            @Value("${spring.data.redis.port}") int redisPort) {
        // Spring에서 사용할 Redis 연결 팩토리
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // RedisTemplate Bean 설정
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new GenericToStringSerializer<>(String.class));
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        return template;
    }
}