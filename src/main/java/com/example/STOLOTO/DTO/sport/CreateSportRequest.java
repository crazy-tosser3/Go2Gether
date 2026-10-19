package com.example.STOLOTO.DTO.sport;

import com.example.STOLOTO.Models.SportCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSportRequest(@NotBlank @Size(max = 60) String name, SportCategory category) {
}
