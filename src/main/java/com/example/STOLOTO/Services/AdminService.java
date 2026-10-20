package com.example.STOLOTO.Services;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.admin.CacheStatsResponse;
import com.example.STOLOTO.DTO.admin.DashboardResponse;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.OrganizationStatus;
import com.example.STOLOTO.repository.CommentRepository;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.cache.PageCache;
import com.example.STOLOTO.repository.UserRepository;

import io.micrometer.core.instrument.MeterRegistry;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminService {

    private final OrganizationRepository organizations;
    private final EventRepository events;
    private final CommentRepository comments;
    private final UserRepository users;
    private final PageCache pageCache;
    private final CacheManager cacheManager;
    private final ObjectProvider<MeterRegistry> meters;

    public DashboardResponse dashboard() {
        return new DashboardResponse(
                organizations.countByStatus(OrganizationStatus.PENDING),
                organizations.countByStatus(OrganizationStatus.APPROVED),
                events.countByStatus(EventStatus.PUBLISHED),
                events.countByStatus(EventStatus.UNPUBLISHED),
                events.countByStatus(EventStatus.DELETED),
                events.countByStatus(EventStatus.CANCELLED),
                comments.countByHiddenTrue(),
                users.count());
    }

    public void flushCache() {
        pageCache.flushAll();
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    public CacheStatsResponse cacheStats() {
        Map<String, long[]> byRegion = new TreeMap<>();
        MeterRegistry registry = meters.getIfAvailable();
        if (registry != null) {
            registry.find("stoloto.page.cache").counters().forEach(c -> {
                long[] v = byRegion.computeIfAbsent(c.getId().getTag("region"), k -> new long[3]);
                int i = switch (c.getId().getTag("result")) {
                    case "hit" -> 0;
                    case "miss" -> 1;
                    default -> 2;
                };
                v[i] += (long) c.count();
            });
        }
        var regions = byRegion.entrySet().stream()
                .map(e -> new CacheStatsResponse.Region(e.getKey(), e.getValue()[0], e.getValue()[1],
                        e.getValue()[2], rate(e.getValue()[0], e.getValue()[1])))
                .toList();
        long hits = regions.stream().mapToLong(CacheStatsResponse.Region::hits).sum();
        long misses = regions.stream().mapToLong(CacheStatsResponse.Region::misses).sum();
        return new CacheStatsResponse(regions, hits, misses, rate(hits, misses));
    }

    private static double rate(long hits, long misses) {
        return hits + misses == 0 ? 0 : Math.round(1000.0 * hits / (hits + misses)) / 1000.0;
    }
}
