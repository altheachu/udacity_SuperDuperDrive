package com.udacity.jwdnd.course1.cloudstorage.services;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private CacheManager cacheManager;

    public CacheService(CacheManager cacheManager){
        this.cacheManager = cacheManager;
    }

    @CacheEvict(cacheNames = "user", allEntries = true, beforeInvocation = true)
    public String clearAll(){
        return "clear all keys in cache named 'user'.";
    }
}
