package com.example.STOLOTO.DTO.notification;

import java.time.Instant;

import com.example.STOLOTO.Models.NotificationType;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String body,
        Long eventId,
        boolean isRead,
        Instant createdAt) {
}
