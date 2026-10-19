package com.example.STOLOTO.DTO.event;

import java.time.Instant;

import com.example.STOLOTO.DTO.sport.SportResponse;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.JoinMode;
import com.example.STOLOTO.Models.Level;

public record EventShortResponse(
        String id,
        String title,
        SportResponse sport,
        OrganizerResponse organizer,
        Instant startsAt,
        Instant endsAt,
        String city,
        String address,
        Level level,
        JoinMode joinMode,
        Integer capacity,
        long confirmedCount,
        EventStatus status,
        boolean isPast,
        boolean isEdited) {
}
