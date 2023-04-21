package com.authservice;

import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void save(String token, long expiration) {
        redisTemplate.opsForValue().set(token, token, Duration.ofSeconds(expiration));
    }

    public boolean exists(String token) {
        return redisTemplate.hasKey(token);
    }

    public void delete(String token) {
        redisTemplate.delete(token);
    }
}
