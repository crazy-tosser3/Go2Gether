package com.example.STOLOTO.DTO.user;

import java.util.List;
import java.util.Map;

import com.example.STOLOTO.Models.Level;
import com.example.STOLOTO.Models.TimeSlot;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 1, max = 100) String name,
        @Size(max = 100) String city,
        @Size(max = 2000) String bio,
        @Size(max = 500) String avatarUrl,
        @Size(max = 50) List<Long> sportIds,
        Level level,
        Integer birthYear,
        @Size(max = 50) Map<Long, Level> sportLevels,
        @Min(1) @Max(50) Integer radiusKm,
        @Size(max = 4) List<TimeSlot> preferredTimes,
        @Min(10) @Max(99) Integer preferredAgeFrom,
        @Min(10) @Max(99) Integer preferredAgeTo) {
}
