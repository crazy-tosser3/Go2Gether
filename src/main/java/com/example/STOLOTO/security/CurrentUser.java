package com.example.STOLOTO.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/** Доступ к текущему пользователю из JWT (sub = id пользователя). */
@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UserRepository users;

    public Optional<Long> id() {
        return token().map(t -> Long.valueOf(t.getToken().getSubject()));
    }

    public Optional<String> tokenId() {
        return token().map(t -> t.getToken().getId());
    }

    /** Деактивированный пользователь (is_active = false) считается неавторизованным. */
    public Optional<User> find() {
        return id().flatMap(users::findById).filter(User::isActive);
    }

    public User require() {
        return find().orElseThrow(ApiException::unauthorized);
    }

    public boolean isAdmin() {
        return find().map(u -> u.getRole() == Role.ADMIN).orElse(false);
    }

    private Optional<JwtAuthenticationToken> token() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth instanceof JwtAuthenticationToken jwt ? Optional.of(jwt) : Optional.empty();
    }
}
