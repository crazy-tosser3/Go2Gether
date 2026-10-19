package com.example.STOLOTO.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Кэш в Redis.
 * <ul>
 *   <li>Spring Cache (spring.cache.type=redis): каталог направлений, демо-аккаунты.</li>
 *   <li>{@link com.example.STOLOTO.cache.PageCache}: готовые ответы горячих страниц
 *       (лента, карточка события, участники, обсуждение, организации, профили).</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
