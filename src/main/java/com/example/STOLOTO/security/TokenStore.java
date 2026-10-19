package com.example.STOLOTO.security;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenStore {

    private static final String PREFIX = "auth:token:";

    private final StringRedisTemplate redis;

    public void save(String jti, String userId, Duration ttl) {
        redis.opsForValue().set(PREFIX + jti, userId, ttl);
    }

    public boolean isActive(String jti) {
        return jti != null && Boolean.TRUE.equals(redis.hasKey(PREFIX + jti));
    }

    public void revoke(String jti) {
        redis.delete(PREFIX + jti);
    }
}
