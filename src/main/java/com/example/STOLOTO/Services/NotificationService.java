package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.notification.NotificationResponse;
import com.example.STOLOTO.Models.Notification;
import com.example.STOLOTO.Models.NotificationType;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.repository.NotificationRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notifications;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    public void notify(Long recipientId, NotificationType type, String title, String body, Long eventId) {
        if (recipientId == null) {
            return;
        }
        Notification n = new Notification();
        n.setRecipientId(recipientId);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setEventId(eventId);
        n.setCreatedAt(Instant.now());
        notifications.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list() {
        return notifications.findByRecipientIdOrderByCreatedAtDesc(currentUser.require().getId()).stream()
                .map(mapper::toNotification)
                .toList();
    }

    public NotificationResponse markRead(Long id) {
        Notification n = notifications.findByIdAndRecipientId(id, currentUser.require().getId())
                .orElseThrow(() -> ApiException.notFound("Уведомление не найдено"));
        n.setRead(true);
        return mapper.toNotification(n);
    }
}
