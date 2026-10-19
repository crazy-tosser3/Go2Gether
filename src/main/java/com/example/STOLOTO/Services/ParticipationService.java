package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.participation.JoinResponse;
import com.example.STOLOTO.DTO.participation.ParticipantResponse;
import com.example.STOLOTO.DTO.participation.ParticipantsResponse;
import com.example.STOLOTO.DTO.participation.ParticipationResponse;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.JoinMode;
import com.example.STOLOTO.Models.NotificationType;
import com.example.STOLOTO.Models.Participation;
import com.example.STOLOTO.Models.ParticipationStatus;
import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.ParticipationRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participations;
    private final EventRepository events;
    private final EventService eventService;
    private final NotificationService notificationService;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    /**
     * POST /events/{id}/join — логика со стр. 3 схемы. Строка события блокируется (SELECT ... FOR UPDATE),
     * чтобы лимит мест не превысили параллельные записи.
     */
    public JoinResponse join(Long eventId, String message) {
        User me = currentUser.require();
        Event e = lockEvent(eventId);
        if (e.getStatus() != EventStatus.PUBLISHED) {
            throw EventService.unavailable(e);
        }
        if (e.isPast()) {
            throw ApiException.conflict("EVENT_PAST", "Событие уже прошло");
        }
        if (mapper.isOrganizer(e, me.getId())) {
            throw ApiException.conflict("OWN_EVENT", "Нельзя записаться на своё событие");
        }
        // UNIQUE(event_id, user_id): после rejected/cancelled переиспользуем ту же строку
        Participation p = participations.findByEventIdAndUserId(eventId, me.getId()).orElse(null);
        if (p != null && p.getStatus().isActive()) {
            throw ApiException.conflict("ALREADY_JOINED", "У вас уже есть активная запись",
                    Map.of("participation_id", p.getId().toString(), "status", p.getStatus().name().toLowerCase()));
        }
        Instant now = Instant.now();
        if (p == null) {
            p = new Participation();
            p.setEventId(eventId);
            p.setUserId(me.getId());
            p.setCreatedAt(now);
        }
        p.setMessage(message);
        p.setDecisionReason(null);
        p.setDecidedAt(null);
        p.setWaitlistPosition(null);
        p.setUpdatedAt(now);

        String text;
        if (isFull(e)) {
            p.setStatus(ParticipationStatus.WAITLIST);
            p.setWaitlistPosition(participations.maxWaitlistPosition(eventId) + 1);
            text = "Мест нет — вы в листе ожидания";
        } else if (e.getJoinMode() == JoinMode.OPEN) {
            p.setStatus(ParticipationStatus.CONFIRMED);
            p.setDecidedAt(now);
            text = "Вы записаны";
        } else {
            p.setStatus(ParticipationStatus.PENDING);
            text = "Заявка на рассмотрении";
            notificationService.notify(mapper.organizerRecipient(e), NotificationType.NEW_REGISTRATION,
                    "Новая заявка", me.getName() + " хочет участвовать в «" + e.getTitle() + "»", eventId);
        }
        participations.save(p);
        var details = p.getStatus() == ParticipationStatus.CONFIRMED ? mapper.toEventDetails(e, me) : null;
        return new JoinResponse(text, mapper.toParticipation(p), details);
    }

    /** DELETE /events/{id}/join — отменить свою запись/заявку. */
    public ParticipationResponse leave(Long eventId) {
        User me = currentUser.require();
        Event e = lockEvent(eventId);
        Participation p = participations.findByEventIdAndUserId(eventId, me.getId())
                .filter(x -> x.getStatus().isActive())
                .orElseThrow(() -> ApiException.notFound("Активная запись не найдена"));
        boolean freedSeat = p.getStatus() == ParticipationStatus.CONFIRMED;
        p.setStatus(ParticipationStatus.CANCELLED);
        p.setWaitlistPosition(null);
        p.setUpdatedAt(Instant.now());
        participations.flush();
        if (freedSeat) {
            promoteFromWaitlist(e);
        }
        return mapper.toParticipation(p);
    }

    @Transactional(readOnly = true)
    public ParticipationResponse myParticipation(Long eventId) {
        User me = currentUser.require();
        eventService.require(eventId);
        return participations.findByEventIdAndUserId(eventId, me.getId()).map(mapper::toParticipation).orElse(null);
    }

    /** Подтверждённые участники. Публично — имена и количество; организатору и админу — контакты. */
    @Transactional(readOnly = true)
    public ParticipantsResponse participants(Long eventId) {
        Event e = eventService.requireViewable(eventId);
        boolean privileged = currentUser.find()
                .map(u -> u.getRole() == Role.ADMIN || mapper.isOrganizer(e, u.getId()))
                .orElse(false);
        var list = participations.findByEventIdAndStatusOrderByCreatedAtAsc(eventId, ParticipationStatus.CONFIRMED)
                .stream()
                .map(p -> mapper.toParticipant(p, privileged))
                .toList();
        return new ParticipantsResponse(list.size(), e.getCapacity(), list);
    }

    /** Заявки на рассмотрение: организатор (админ — только просмотр). */
    @Transactional(readOnly = true)
    public List<ParticipantResponse> requests(Long eventId, ParticipationStatus status) {
        User me = currentUser.require();
        Event e = eventService.require(eventId);
        if (me.getRole() != Role.ADMIN && !mapper.isOrganizer(e, me.getId())) {
            throw ApiException.forbidden("Вы не организатор этого мероприятия");
        }
        ParticipationStatus filter = status == null ? ParticipationStatus.PENDING : status;
        return participations.findByEventIdAndStatusOrderByCreatedAtAsc(eventId, filter).stream()
                .map(p -> mapper.toParticipant(p, true))
                .toList();
    }

    public ParticipationResponse approve(Long participationId) {
        Decision d = decide(participationId);
        if (isFull(d.event())) {
            throw ApiException.conflict("NO_SEATS", "Мест нет");
        }
        Participation p = d.participation();
        p.setStatus(ParticipationStatus.CONFIRMED);
        p.setDecidedAt(Instant.now());
        p.setUpdatedAt(p.getDecidedAt());
        notificationService.notify(p.getUserId(), NotificationType.REGISTRATION_CONFIRMED, "Заявка принята",
                "Вы участник «" + d.event().getTitle() + "»", d.event().getId());
        return mapper.toParticipation(p);
    }

    public ParticipationResponse reject(Long participationId, String reason) {
        Decision d = decide(participationId);
        Participation p = d.participation();
        p.setStatus(ParticipationStatus.REJECTED);
        p.setDecisionReason(reason);
        p.setDecidedAt(Instant.now());
        p.setUpdatedAt(p.getDecidedAt());
        notificationService.notify(p.getUserId(), NotificationType.REGISTRATION_REJECTED, "Заявка отклонена",
                "Заявка на «" + d.event().getTitle() + "» отклонена" + (reason == null ? "" : ": " + reason),
                d.event().getId());
        return mapper.toParticipation(p);
    }

    private record Decision(Participation participation, Event event) {
    }

    /** Общие проверки approve/reject: организатор → pending → событие активно. */
    private Decision decide(Long participationId) {
        User me = currentUser.require();
        Participation p = participations.findById(participationId)
                .orElseThrow(() -> ApiException.notFound("Заявка не найдена"));
        Event e = lockEvent(p.getEventId());
        if (!mapper.isOrganizer(e, me.getId())) {
            throw ApiException.forbidden("Вы не организатор этого мероприятия");
        }
        if (p.getStatus() != ParticipationStatus.PENDING) {
            throw ApiException.conflict("ALREADY_REVIEWED", "Заявка уже рассмотрена",
                    Map.of("status", p.getStatus().name().toLowerCase()));
        }
        if (e.getStatus() != EventStatus.PUBLISHED) {
            throw EventService.unavailable(e);
        }
        if (e.isPast()) {
            throw ApiException.conflict("EVENT_PAST", "Событие уже прошло");
        }
        return new Decision(p, e);
    }

    private Event lockEvent(Long eventId) {
        return events.findByIdForUpdate(eventId).orElseThrow(() -> ApiException.notFound("Мероприятие не найдено"));
    }

    private boolean isFull(Event e) {
        return e.getCapacity() != null && mapper.count(e.getId(), ParticipationStatus.CONFIRMED) >= e.getCapacity();
    }

    /** Место освободилось: первый в листе ожидания → confirmed (open) или pending (approval). */
    private void promoteFromWaitlist(Event e) {
        if (e.getStatus() != EventStatus.PUBLISHED || e.isPast()) {
            return;
        }
        participations.findFirstByEventIdAndStatusOrderByWaitlistPositionAscCreatedAtAsc(e.getId(),
                ParticipationStatus.WAITLIST).ifPresent(p -> {
                    p.setWaitlistPosition(null);
                    p.setUpdatedAt(Instant.now());
                    if (e.getJoinMode() == JoinMode.OPEN) {
                        p.setStatus(ParticipationStatus.CONFIRMED);
                        p.setDecidedAt(p.getUpdatedAt());
                        notificationService.notify(p.getUserId(), NotificationType.WAITLIST_PROMOTED,
                                "Место освободилось", "Вы участник «" + e.getTitle() + "»", e.getId());
                    } else {
                        p.setStatus(ParticipationStatus.PENDING);
                        notificationService.notify(mapper.organizerRecipient(e), NotificationType.NEW_REGISTRATION,
                                "Заявка из листа ожидания", "Освободилось место в «" + e.getTitle() + "»",
                                e.getId());
                    }
                });
    }
}
