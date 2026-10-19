package com.example.STOLOTO.DTO.event;

import java.time.Instant;

import com.example.STOLOTO.Models.JoinMode;
import com.example.STOLOTO.Models.Level;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateEventRequest(
        @Size(min = 1, max = 200) String title,
        @Size(max = 10000) String description,
        String sportId,
        @Size(max = 60) String sportName,
        @Future Instant startsAt,
        Instant endsAt,
        @Size(max = 100) String city,
        @Size(min = 1, max = 500) String address,
        @Size(max = 1000) String locationNote,
        @DecimalMin("-90") @DecimalMax("90") Double lat,
        @DecimalMin("-180") @DecimalMax("180") Double lng,
        Level level,
        @Size(max = 5000) String requirements,
        @Size(max = 5000) String equipment,
        JoinMode joinMode,
        @Min(1) Integer capacity) {
}
