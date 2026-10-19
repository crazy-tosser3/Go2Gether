package com.example.STOLOTO.DTO.user;

import java.util.List;

import com.example.STOLOTO.DTO.event.EventShortResponse;
import com.example.STOLOTO.DTO.sport.SportResponse;
import com.example.STOLOTO.Models.Level;

public record PublicUserResponse(
        Long id,
        String name,
        String avatarUrl,
        String city,
        String bio,
        Level level,
        List<SportResponse> sports,
        List<EventShortResponse> createdEvents) {
}
