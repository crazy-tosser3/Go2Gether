package com.example.STOLOTO.DTO.safety;

import java.io.Serializable;
import java.util.List;

import com.example.STOLOTO.Models.SafetyCategory;

public record SafetyBannerDetails(
        Long id,
        String title,
        String subtitle,
        SafetyCategory category,
        String coverUrl,
        String accentColor,
        Long sportId,
        boolean published,
        List<SafetySlideResponse> slides,
        String disclaimer) implements Serializable {
}