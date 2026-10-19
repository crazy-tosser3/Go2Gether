package com.example.STOLOTO.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final int MAX_IP_LENGTH = 45;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String ip = getClientIp(request);
        String path = request.getRequestURI();
        String method = request.getMethod();
        String key = ip + ":" + getBucketType(path, method);

        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(path, method));

        if (bucket.tryConsume(1)) {
            response.setHeader("X-RateLimit-Remaining",
                    String.valueOf(bucket.getAvailableTokens()));
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "10");
            response.getWriter().write(
                    objectMapper.writeValueAsString(Map.of(
                            "error", "TOO_MANY_REQUESTS",
                            "message", "Слишком много запросов. Попробуйте через несколько секунд."
                    ))
            );
        }
    }

    private String getBucketType(String path, String method) {
        if (path.contains("/auth/login") || path.contains("/auth/register")) {
            return "auth";
        }
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            return "read";
        }
        return "write";
    }

    private Bucket createBucket(String path, String method) {
        String type = getBucketType(path, method);
        Bandwidth limit = switch (type) {
            // 10 запросов в минуту — защита от перебора паролей
            case "auth"  -> Bandwidth.classic(10,
                    Refill.greedy(10, Duration.ofMinutes(1)));
            // 60 запросов в минуту — запись
            case "write" -> Bandwidth.classic(60,
                    Refill.greedy(60, Duration.ofMinutes(1)));
            // 300 запросов в минуту — чтение (жюри не заблокируем)
            default      -> Bandwidth.classic(300,
                    Refill.greedy(300, Duration.ofMinutes(1)));
        };
        return Bucket.builder().addLimit(limit).build();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String ip = forwarded.split(",")[0].trim();
            if (ip.length() > MAX_IP_LENGTH) {
                ip = ip.substring(0, MAX_IP_LENGTH);
            }
            return ip;
        }
        return request.getRemoteAddr();
    }

    @Scheduled(fixedDelay = 3_600_000)
    public void cleanUp() {
        int before = buckets.size();
        buckets.entrySet().removeIf(entry ->
                entry.getValue().getAvailableTokens() >= 298);
        log.info("RateLimit cleanup: {} → {} buckets", before, buckets.size());
    }
}