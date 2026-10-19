package com.example.STOLOTO.DTO.participation;

import java.time.Instant;

import com.example.STOLOTO.DTO.user.UserShortResponse;
import com.example.STOLOTO.Models.ParticipationStatus;

public record ParticipantResponse(
        Long participationId,
        UserShortResponse user,
        String email,
        ParticipationStatus status,
        String message,
        String decisionReason,
        Instant createdAt) {
}
