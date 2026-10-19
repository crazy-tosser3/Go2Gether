package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.STOLOTO.DTO.notification.NotificationResponse;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.Models.Notification;
import com.example.STOLOTO.Models.NotificationType;
import com.example.STOLOTO.repository.NotificationRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notifications;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    public void notify(String userId, NotificationType type, Map<String, String> payload) {
        if (userId == null) {
            return;
        }
        Notification n = new Notification();
        n.setId(UUID.randomUUID().toString());
        n.setUserId(userId);
        n.setType(type);
        n.setPayload(new HashMap<>(payload));
        n.setCreatedAt(Instant.now());
        notifications.save(n);
    }

    public List<NotificationResponse> list() {
        return notifications.findByUserId(currentUser.require().getId()).stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .map(mapper::toNotification)
                .toList();
    }

    public NotificationResponse markRead(String id) {
        String me = currentUser.require().getId();
        Notification n = notifications.findById(id)
                .filter(x -> me.equals(x.getUserId()))
                .orElseThrow(() -> ApiException.notFound("Уведомление не найдено"));
        n.setRead(true);
        return mapper.toNotification(notifications.save(n));
    }
}
