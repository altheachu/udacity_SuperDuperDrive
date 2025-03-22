package com.udacity.jwdnd.course1.cloudstorage.services;

import com.udacity.jwdnd.course1.cloudstorage.entity.User;
import com.udacity.jwdnd.course1.cloudstorage.mapper.UserMapper;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.function.Supplier;

@Service
public class UserService {

    private UserMapper userMapper;
    private HashService hashService;
    private CacheService cacheService;

    private RedisService redisService;


    public UserService(UserMapper userMapper, HashService hashService, CacheService cacheService, RedisService redisService){
        this.userMapper = userMapper;
        this.hashService = hashService;
        this.cacheService = cacheService;
        this.redisService = redisService;
    }

    public boolean isUsernameAvailable(String username){
        boolean isUsernameAvailable = true;
        User user = userMapper.findUser(username);
        if(user!=null){
            isUsernameAvailable = false;
        }
        return isUsernameAvailable;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer createUser(User user, boolean saveCache, boolean useRedisTemplate){
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        // make salt become a format that hash can understand
        String encodedSalt = Base64.getEncoder().encodeToString(salt);
        String hashedPassword = hashService.getHashedValue(user.getPassword(), encodedSalt);
        user.setSalt(encodedSalt);
        user.setPassword(hashedPassword);
        int result = userMapper.createUser(user);
        if(useRedisTemplate){
            redisService.setString(user.getUsername(), user.getUserId());
        } else {
            cacheService.putUserInfoIntoCache(user, saveCache);
        }
        return result;
    }

    @Cacheable(cacheNames = "user", key = "#username", unless = "#result.startsWith('test-')")
    public Integer findUserIdByUsername(String username){
        Integer userId = 0;
        User user = userMapper.findUser(username);
        if(user!=null){
            userId = user.getUserId();
        }
        return userId;
    }

    public Integer findUserIdByRedisTemplate(String username){
        Supplier<String> userSupplier = () -> {
            User user = userMapper.findUser(username);
            Integer userId = user!=null? user.getUserId(): 0;
            return String.valueOf(userId);
        };
        String userId = redisService.getString(username, userSupplier);
        return Integer.parseInt(userId);
    }

}
