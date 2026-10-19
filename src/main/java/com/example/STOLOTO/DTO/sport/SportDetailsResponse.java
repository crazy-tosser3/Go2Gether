package com.example.STOLOTO.DTO.sport;

import com.example.STOLOTO.Models.SportCategory;

public record SportDetailsResponse(
        Long id,
        String name,
        SportCategory category,
        String icon,
        boolean isCustom,
        long activeEventsCount) {
}
