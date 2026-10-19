package com.example.STOLOTO.DTO.user;

import java.util.List;

import com.example.STOLOTO.Models.Level;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 1, max = 100) String name,
        @Size(max = 100) String city,
        @Size(max = 2000) String bio,
        @Size(max = 500) String avatarUrl,
        @Size(max = 50) List<Long> sportIds,
        Level level) {
}
