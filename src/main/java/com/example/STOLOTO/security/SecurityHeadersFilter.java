package com.example.STOLOTO.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1) // Выполняется первым — до всех остальных фильтров
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        // Запрещаем встраивать сайт в iframe (защита от clickjacking)
        response.setHeader("X-Frame-Options", "DENY");

        // Браузер не должен угадывать Content-Type (защита от MIME-sniffing атак)
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Только HTTPS, 1 год (включается когда у вас SSL)
        response.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains");

        // Политика безопасности контента — разрешаем только свои ресурсы
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                        "script-src 'self'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data:; " +
                        "frame-ancestors 'none'; " +
                        "form-action 'self'");

        // Не отправляем Referer на другие сайты
        response.setHeader("Referrer-Policy",
                "strict-origin-when-cross-origin");

        // Запрещаем браузеру включать микрофон/камеру/геолокацию без спроса
        response.setHeader("Permissions-Policy",
                "camera=(), microphone=(), geolocation=()");

        chain.doFilter(request, response);
    }
}