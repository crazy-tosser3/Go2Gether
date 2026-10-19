package com.example.STOLOTO.DTO.auth;

import com.example.STOLOTO.DTO.user.UserResponse;

public record AuthResponse(String token, String tokenType, long expiresIn, UserResponse user) {

    public static AuthResponse bearer(String token, long expiresIn, UserResponse user) {
        return new AuthResponse(token, "Bearer", expiresIn, user);
    }
}
