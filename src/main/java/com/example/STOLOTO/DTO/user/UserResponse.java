package com.example.STOLOTO.DTO.user;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.example.STOLOTO.Models.Level;
import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.TimeSlot;

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
        Instant createdAt,
        Integer birthYear,
        Map<Long, Level> sportLevels,
        Integer radiusKm,
        List<TimeSlot> preferredTimes,
        Integer preferredAgeFrom,
        Integer preferredAgeTo) {
}
