package com.example.STOLOTO.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/** Кэш в Redis (spring.cache.type=redis): каталог направлений и демо-аккаунты. */
@Configuration
@EnableCaching
public class CacheConfig {
}
