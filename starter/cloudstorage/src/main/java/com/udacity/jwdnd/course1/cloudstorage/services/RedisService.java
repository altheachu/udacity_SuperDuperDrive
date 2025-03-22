package com.udacity.jwdnd.course1.cloudstorage.services;

import io.netty.util.internal.StringUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class RedisService {

    private RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public String getString(String key, Supplier<String> dataSupplier){
        String cache = redisTemplate.opsForValue().get(key);
        if(!StringUtils.isEmpty(cache)){
            return cache;
        }
        String data = dataSupplier.get();
        redisTemplate.opsForValue().set(key, data/*, 10, TimeUnit.MINUTES*/);
        return data;
    }

    public void setString(String key, Integer userId){
        redisTemplate.opsForValue().set(key, String.valueOf(userId));
    }

    public void deleteString(String key){
        redisTemplate.delete(key);
    }
}
