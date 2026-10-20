package com.example.STOLOTO.DTO.safety;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SafetySlideRequest(
        @Size(max = 120) String title,
        @NotBlank @Size(max = 2000) String body,
        @Size(max = 500) String imageUrl) {
}