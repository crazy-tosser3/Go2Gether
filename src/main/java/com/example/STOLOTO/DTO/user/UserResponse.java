package com.example.STOLOTO.DTO.user;

import java.time.Instant;
import java.util.List;

import com.example.STOLOTO.Models.Level;
import com.example.STOLOTO.Models.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        String avatarUrl,
        String city,
        String bio,
        Level level,
        List<Long> sportIds,
        boolean isDemo,
        Instant createdAt) {
}
