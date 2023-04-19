package com.authservice;

import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private final Logger logger = LoggerFactory.getLogger(RedisService.class);
    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToBlacklist(String token) throws RedisException {
        try {
            redisTemplate.opsForValue().set(token, "blacklisted", Duration.ofSeconds(360000));
        } catch (Exception e) {
            logger.error("Error adding token to blacklist: {}", e.getMessage());
            throw new RedisException("Error adding token to blacklist");
        }
    }

    public boolean isBlacklisted(String token) throws RedisException {
        try {
            logger.info("Checking if token is blacklisted: {}", token);
            return redisTemplate.hasKey(token);
        } catch (Exception e) {
            logger.error("Error checking if token is blacklisted: {}", e.getMessage());
            throw new RedisException("Error checking if token is blacklisted");
        }
    }
}
