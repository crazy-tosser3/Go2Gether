package com.example.STOLOTO.DTO.auth;

import com.example.STOLOTO.DTO.user.UserResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record AuthResponse(
        @JsonIgnore String token,   // не отдаём токен в JSON — он идёт в куки
        String tokenType,
        long expiresIn,
        UserResponse user) {

    public static AuthResponse bearer(String token, long expiresIn, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresIn, user);
    }
}
