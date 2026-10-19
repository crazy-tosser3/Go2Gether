package com.example.STOLOTO.DTO.event;

import java.time.Instant;

import com.example.STOLOTO.Models.JoinMode;
import com.example.STOLOTO.Models.Level;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEventRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 10000) String description,
        String sportId,
        @Size(max = 60) String sportName,
        @NotNull @Future Instant startsAt,
        Instant endsAt,
        @Size(max = 100) String city,
        @NotBlank @Size(max = 500) String address,
        @Size(max = 1000) String locationNote,
        @DecimalMin("-90") @DecimalMax("90") Double lat,
        @DecimalMin("-180") @DecimalMax("180") Double lng,
        Level level,
        @Size(max = 5000) String requirements,
        @Size(max = 5000) String equipment,
        @NotNull JoinMode joinMode,
        @Min(1) Integer capacity,
        String organizationId) {
}
