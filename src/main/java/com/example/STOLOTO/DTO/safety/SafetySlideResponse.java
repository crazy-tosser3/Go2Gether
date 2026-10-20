package com.example.STOLOTO.DTO.safety;

import java.io.Serializable;

public record SafetySlideResponse(
        int position,
        String title,
        String body,
        String imageUrl) implements Serializable {
}