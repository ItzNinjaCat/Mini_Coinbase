package com.authservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

public class RedisExpiredListener implements MessageListener {
    @Autowired
    private RedisService redisService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());

        if (redisService.exists(expiredKey)) {
            redisService.delete(expiredKey);
        }
    }
}
