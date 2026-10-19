package com.example.STOLOTO.DTO.admin;

import java.util.List;

/** Статистика кэша страниц с момента запуска приложения. */
public record CacheStatsResponse(List<Region> regions, long hits, long misses, double hitRate) {

    public record Region(String region, long hits, long misses, long errors, double hitRate) {
    }
}
