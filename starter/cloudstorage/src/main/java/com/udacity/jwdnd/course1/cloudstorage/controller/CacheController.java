package com.udacity.jwdnd.course1.cloudstorage.controller;

import com.udacity.jwdnd.course1.cloudstorage.services.CacheService;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ConcurrentMap;

@RequestMapping("/cache")
@Controller
public class CacheController {

    private CacheService cacheService;
    private CacheManager cacheManager;

    public CacheController(CacheService cacheService, CacheManager cacheManager){
        this.cacheService = cacheService;
        this.cacheManager = cacheManager;
    }

    @PostMapping("/clear")
    public String clearAllCache(){
        try {
            System.out.println(cacheService.clearAll());
            // test code
            /*
            ConcurrentMap<Object, Object> cacheMap = (ConcurrentMap<Object, Object>) cacheManager.getCache("user").getNativeCache();
            System.out.println(cacheMap.keySet().size());
            */
        }finally {
            return "/home";
        }

    }
}
