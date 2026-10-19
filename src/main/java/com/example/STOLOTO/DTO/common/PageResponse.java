package com.example.STOLOTO.DTO.common;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int size, long total, int totalPages) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public static <T> PageResponse<T> of(List<T> all, Integer page, Integer size) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int from = (int) Math.min((long) p * s, all.size());
        int to = Math.min(from + s, all.size());
        int totalPages = (all.size() + s - 1) / s;
        return new PageResponse<>(List.copyOf(all.subList(from, to)), p, s, all.size(), totalPages);
    }
}
