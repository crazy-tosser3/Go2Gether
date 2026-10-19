package com.example.STOLOTO.DTO.participation;

import java.time.Instant;

import com.example.STOLOTO.Models.ParticipationStatus;

public record ParticipationResponse(
        Long id,
        Long eventId,
        Long userId,
        ParticipationStatus status,
        String message,
        String decisionReason,
        Instant decidedAt,
        Instant createdAt) {
}
