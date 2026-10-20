package com.example.STOLOTO.DTO.safety;

import java.io.Serializable;

import com.example.STOLOTO.Models.SafetyCategory;

public record SafetyBannerCard(
        Long id,
        String title,
        String subtitle,
        SafetyCategory category,
        String coverUrl,
        String accentColor,
        Long sportId,
        boolean published) implements Serializable {
}