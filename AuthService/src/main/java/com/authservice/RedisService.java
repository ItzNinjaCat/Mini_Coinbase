package com.authservice;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
@Slf4j
@Service
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void save(String token, long expiration) {
        redisTemplate.opsForValue().set(token, token, Duration.ofSeconds(expiration));
        log.info("Token saved in redis");
    }

    public boolean exists(String token) {
        return redisTemplate.hasKey(token);
    }

    public void delete(String token) {
        redisTemplate.delete(token);
    }
}
