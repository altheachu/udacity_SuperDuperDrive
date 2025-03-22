# Redis Mechanism Introduction

## RedisCacheManager versus RedisTemplate Comparison
| Function                                      | RedisCacheManager              | RedisTemplate      |
|-----------------------------------------------|--------------------------------|--------------------|
| Advanced Key Type Support<br/>(List、Set、Hash) | X                             | V                  |
| Customed TTL & Serialization                  | global setting in config       | local case by case |
| Cache Get and Set                             | Through SpringBoot Annotation  | Manual             |
| Redis Transactions (no rollback)              | X                              | V                  |
| Lua Template (For Batch and support rollback) | X                              | V                  |