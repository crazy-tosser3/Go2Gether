package com.example.STOLOTO.DTO.event;

import java.time.Instant;
import java.util.List;

import com.example.STOLOTO.DTO.participation.ParticipationResponse;
import com.example.STOLOTO.DTO.sport.SportResponse;
import com.example.STOLOTO.DTO.user.UserShortResponse;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.JoinMode;
import com.example.STOLOTO.Models.Level;

public record EventDetailsResponse(
        String id,
        String title,
        String description,
        SportResponse sport,
        OrganizerResponse organizer,
        String organizerContacts,
        Instant startsAt,
        Instant endsAt,
        String city,
        String address,
        String locationNote,
        Double lat,
        Double lng,
        Level level,
        String requirements,
        String equipment,
        JoinMode joinMode,
        Integer capacity,
        long confirmedCount,
        List<UserShortResponse> participants,
        EventStatus status,
        String unavailableReason,
        boolean isPast,
        boolean isEdited,
        boolean isOrganizer,
        ParticipationResponse myParticipation,
        Instant createdAt,
        Instant updatedAt) {
}
