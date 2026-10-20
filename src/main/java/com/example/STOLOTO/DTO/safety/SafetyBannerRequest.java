package com.example.STOLOTO.DTO.safety;

import java.util.List;

import com.example.STOLOTO.Models.SafetyCategory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SafetyBannerRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 255) String subtitle,
        @NotNull SafetyCategory category,
        @Size(max = 500) String coverUrl,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Цвет в формате #RRGGBB") String accentColor,
        Long sportId,
        Integer sortOrder,
        Boolean published,
        @NotEmpty @Size(max = 20) List<@Valid SafetySlideRequest> slides) {
}