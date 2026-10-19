package com.example.STOLOTO.DTO.event;

import com.example.STOLOTO.Models.OrganizerType;

public record OrganizerResponse(OrganizerType type, String id, String name, String avatarUrl) {
}
