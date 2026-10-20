package com.example.STOLOTO.security;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.STOLOTO.Controller.AuthController;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

public class StaleJwtCookieFilter extends OncePerRequestFilter {

    private static final String COOKIE_NAME = "jwt";

    private final JwtDecoder jwtDecoder;

    public StaleJwtCookieFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Cookie jwt = findJwtCookie(request);
        if (jwt == null || isValid(jwt.getValue())) {
            chain.doFilter(request, response);
            return;
        }
        response.addHeader(HttpHeaders.SET_COOKIE, AuthController.clearCookie().toString());
        chain.doFilter(new WithoutJwtCookie(request), response);
    }

    private boolean isValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private static Cookie findJwtCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_NAME.equals(c.getName()))
                .findFirst()
                .orElse(null);
    }

    private static final class WithoutJwtCookie extends HttpServletRequestWrapper {

        WithoutJwtCookie(HttpServletRequest request) {
            super(request);
        }

        @Override
        public Cookie[] getCookies() {
            Cookie[] all = super.getCookies();
            if (all == null) {
                return null;
            }
            Cookie[] rest = Arrays.stream(all)
                    .filter(c -> !COOKIE_NAME.equals(c.getName()))
                    .toArray(Cookie[]::new);
            return rest.length == 0 ? null : rest;
        }
    }
}