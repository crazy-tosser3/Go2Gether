package com.example.STOLOTO.DTO.sport;

import com.example.STOLOTO.Models.SportCategory;

public record SportResponse(String id, String name, SportCategory category, String icon, boolean isCustom) {
}
