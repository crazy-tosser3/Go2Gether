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
        Long id,
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
            
    public EventDetailsResponse forViewer(boolean fullInfo, boolean organizer, ParticipationResponse mine) {
        return new EventDetailsResponse(id, title, description, sport, this.organizer,
                fullInfo ? organizerContacts : null, startsAt, endsAt, city, address,
                fullInfo ? locationNote : null, fullInfo ? lat : null, fullInfo ? lng : null, level, requirements,
                equipment, joinMode, capacity, confirmedCount, participants, status, unavailableReason, isPast,
                isEdited, organizer, mine, createdAt, updatedAt);
    }
}
