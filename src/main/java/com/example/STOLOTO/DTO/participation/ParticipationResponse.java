package com.example.STOLOTO.DTO.participation;

import java.time.Instant;

import com.example.STOLOTO.Models.ParticipationStatus;

public record ParticipationResponse(
        String id,
        String eventId,
        String userId,
        ParticipationStatus status,
        String message,
        String decisionReason,
        Instant decidedAt,
        Instant createdAt) {
}
