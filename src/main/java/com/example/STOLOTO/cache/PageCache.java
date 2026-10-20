package com.example.STOLOTO.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.micrometer.core.instrument.MeterRegistry;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
public class PageCache {

    private static final Logger log = LoggerFactory.getLogger(PageCache.class);
    private static final String PAGE = "stoloto:page:";
    private static final String VERSION = "stoloto:ver:";
    private static final String GLOBAL = "_global";
    private static final Duration VERSION_TTL = Duration.ofDays(1);

    private final StringRedisTemplate redis;
    private final JsonMapper json;
    private final ObjectProvider<MeterRegistry> meters;

    public PageCache(StringRedisTemplate redis, JsonMapper json, ObjectProvider<MeterRegistry> meters) {
        this.redis = redis;
        this.json = json;
        this.meters = meters;
    }

    public <T> T get(String region, String key, Duration ttl, TypeReference<T> type, Supplier<T> loader) {
        String fullKey;
        try {
            List<String> versions = redis.opsForValue().multiGet(List.of(VERSION + GLOBAL, VERSION + region));
            fullKey = PAGE + "g" + version(versions, 0) + ":" + region + ":v" + version(versions, 1) + ":" + safe(key);
            String cached = redis.opsForValue().get(fullKey);
            if (cached != null) {
                record(region, "hit");
                return json.readValue(cached, type);
            }
        } catch (RuntimeException e) {
            log.warn("Кэш {} недоступен, читаю из БД: {}", region, e.getMessage());
            record(region, "error");
            return loader.get();
        }
        record(region, "miss");
        T value = loader.get();
        if (value != null) {
            try {
                redis.opsForValue().set(fullKey, json.writeValueAsString(value), ttl);
            } catch (RuntimeException e) {
                log.warn("Не удалось записать {} в кэш: {}", fullKey, e.getMessage());
            }
        }
        return value;
    }

    public void invalidate(String... regions) {
        Set<String> unique = new LinkedHashSet<>(List.of(regions));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    bump(unique);
                }
            });
        } else {
            bump(unique);
        }
    }

    public void flushAll() {
        bump(Set.of(GLOBAL));
    }

    private void bump(Set<String> regions) {
        try {
            List<String> keys = new ArrayList<>();
            regions.forEach(r -> keys.add(VERSION + r));
            redis.executePipelined((RedisCallback<Object>) connection -> {
                for (String k : keys) {
                    byte[] raw = k.getBytes(StandardCharsets.UTF_8);
                    connection.stringCommands().incr(raw);
                    connection.keyCommands().expire(raw, VERSION_TTL.toSeconds());
                }
                return null;
            });
        } catch (RuntimeException e) {
            log.warn("Не удалось инвалидировать кэш {}: {}", regions, e.getMessage());
        }
    }

    private void record(String region, String result) {
        String group = region.contains(":") ? region.substring(0, region.indexOf(':')) : region;
        MeterRegistry registry = meters.getIfAvailable();
        if (registry != null) {
            registry.counter("stoloto.page.cache", "region", group, "result", result).increment();
        }
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs
                && attrs.getResponse() != null && !attrs.getResponse().isCommitted()) {
            attrs.getResponse().addHeader("X-Cache", group + "=" + result.toUpperCase());
        }
    }

    private static String version(List<String> versions, int i) {
        return versions == null || versions.get(i) == null ? "0" : versions.get(i);
    }

    private static String safe(String key) {
        if (key.length() <= 120) {
            return key;
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            return "h" + HexFormat.of().formatHex(hash, 0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
