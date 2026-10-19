package com.example.STOLOTO.DTO.notification;

import java.time.Instant;
import java.util.Map;

import com.example.STOLOTO.Models.NotificationType;

public record NotificationResponse(
        String id,
        NotificationType type,
        Map<String, String> payload,
        boolean isRead,
        Instant createdAt) {
}
