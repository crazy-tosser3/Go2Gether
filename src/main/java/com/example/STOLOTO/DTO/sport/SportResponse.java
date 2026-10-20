package com.example.STOLOTO.DTO.sport;

import java.io.Serializable;

import com.example.STOLOTO.Models.SportCategory;

public record SportResponse(Long id, String name, SportCategory category, String icon, boolean isCustom)
        implements Serializable {
}
