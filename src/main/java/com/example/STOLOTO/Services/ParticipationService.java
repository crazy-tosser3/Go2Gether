package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.STOLOTO.DTO.participation.JoinResponse;
import com.example.STOLOTO.DTO.participation.ParticipantResponse;
import com.example.STOLOTO.DTO.participation.ParticipantsResponse;
import com.example.STOLOTO.DTO.participation.ParticipationResponse;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.JoinMode;
import com.example.STOLOTO.Models.NotificationType;
import com.example.STOLOTO.Models.Participation;
import com.example.STOLOTO.Models.ParticipationStatus;
import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.ParticipationRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participations;
    private final EventRepository events;
    private final EventService eventService;
    private final NotificationService notificationService;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    public JoinResponse join(String eventId, String message) {
        User me = currentUser.require();
        Event e = eventService.require(eventId);
        if (e.getStatus() != EventStatus.PUBLISHED) {
            throw EventService.unavailable(e);
        }
        if (e.isPast()) {
            throw ApiException.conflict("EVENT_PAST", "Событие уже прошло");
        }
        if (mapper.isOrganizer(e, me.getId())) {
            throw ApiException.conflict("OWN_EVENT", "Нельзя записаться на своё событие");
        }
        List<Participation> existing = participations.findByEventIdAndUserId(eventId, me.getId());
        existing.stream().filter(p -> p.getStatus().isActive()).findFirst().ifPresent(p -> {
            throw ApiException.conflict("ALREADY_JOINED", "У вас уже есть активная запись",
                    Map.of("participation_id", p.getId(), "status", p.getStatus().name().toLowerCase()));
        });

        Participation p = new Participation();
        p.setId(UUID.randomUUID().toString());
        p.setEventId(eventId);
        p.setUserId(me.getId());
        p.setMessage(message);
        p.setCreatedAt(Instant.now());

        String text;
        if (isFull(e)) {
            p.setStatus(ParticipationStatus.WAITLISTED);
            text = "Мест нет — вы в листе ожидания";
        } else if (e.getJoinMode() == JoinMode.OPEN) {
            p.setStatus(ParticipationStatus.CONFIRMED);
            p.setDecidedAt(p.getCreatedAt());
            text = "Вы записаны";
        } else {
            p.setStatus(ParticipationStatus.PENDING);
            text = "Заявка на рассмотрении";
            notificationService.notify(e.getOrganizerUserId(), NotificationType.PARTICIPATION_REQUESTED,
                    Map.of("event_id", eventId, "event_title", e.getTitle(), "participation_id", p.getId(),
                            "user_name", me.getName()));
        }
        participations.save(p);
        var details = p.getStatus() == ParticipationStatus.CONFIRMED ? mapper.toEventDetails(e, me) : null;
        return new JoinResponse(text, mapper.toParticipation(p), details);
    }

    public ParticipationResponse leave(String eventId) {
        User me = currentUser.require();
        Event e = eventService.require(eventId);
        Participation p = participations.findByEventIdAndUserId(eventId, me.getId()).stream()
                .filter(x -> x.getStatus().isActive())
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("Активная запись не найдена"));
        boolean freedSeat = p.getStatus() == ParticipationStatus.CONFIRMED;
        p.setStatus(ParticipationStatus.CANCELLED);
        p.setDecidedAt(Instant.now());
        participations.save(p);
        if (freedSeat) {
            promoteFromWaitlist(e);
        }
        return mapper.toParticipation(p);
    }

    public ParticipationResponse myParticipation(String eventId) {
        User me = currentUser.require();
        eventService.require(eventId);
        Participation p = mapper.latestFor(participations.findByEventId(eventId), me.getId());
        return p == null ? null : mapper.toParticipation(p);
    }

    public ParticipantsResponse participants(String eventId) {
        Event e = eventService.requireViewable(eventId);
        boolean privileged = currentUser.find()
                .map(u -> u.getRole() == Role.ADMIN || mapper.isOrganizer(e, u.getId()))
                .orElse(false);
        var list = participations.findByEventId(eventId).stream()
                .filter(p -> p.getStatus() == ParticipationStatus.CONFIRMED)
                .sorted(Comparator.comparing(Participation::getCreatedAt))
                .map(p -> mapper.toParticipant(p, privileged))
                .toList();
        return new ParticipantsResponse(list.size(), e.getCapacity(), list);
    }

    public List<ParticipantResponse> requests(String eventId, ParticipationStatus status) {
        User me = currentUser.require();
        Event e = eventService.require(eventId);
        if (me.getRole() != Role.ADMIN && !mapper.isOrganizer(e, me.getId())) {
            throw ApiException.forbidden("Вы не организатор этого мероприятия");
        }
        ParticipationStatus filter = status == null ? ParticipationStatus.PENDING : status;
        return participations.findByEventId(eventId).stream()
                .filter(p -> p.getStatus() == filter)
                .sorted(Comparator.comparing(Participation::getCreatedAt))
                .map(p -> mapper.toParticipant(p, true))
                .toList();
    }

    public ParticipationResponse approve(String participationId) {
        Decision d = decide(participationId);
        if (isFull(d.event())) {
            throw ApiException.conflict("NO_SEATS", "Мест нет");
        }
        d.participation().setStatus(ParticipationStatus.CONFIRMED);
        d.participation().setDecidedAt(Instant.now());
        participations.save(d.participation());
        notificationService.notify(d.participation().getUserId(), NotificationType.PARTICIPATION_APPROVED,
                Map.of("event_id", d.event().getId(), "event_title", d.event().getTitle()));
        return mapper.toParticipation(d.participation());
    }

    public ParticipationResponse reject(String participationId, String reason) {
        Decision d = decide(participationId);
        d.participation().setStatus(ParticipationStatus.REJECTED);
        d.participation().setDecisionReason(reason);
        d.participation().setDecidedAt(Instant.now());
        participations.save(d.participation());
        notificationService.notify(d.participation().getUserId(), NotificationType.PARTICIPATION_REJECTED,
                reason == null
                        ? Map.of("event_id", d.event().getId(), "event_title", d.event().getTitle())
                        : Map.of("event_id", d.event().getId(), "event_title", d.event().getTitle(), "reason", reason));
        return mapper.toParticipation(d.participation());
    }

    private record Decision(Participation participation, Event event) {
    }

    private Decision decide(String participationId) {
        User me = currentUser.require();
        Participation p = participations.findById(participationId)
                .orElseThrow(() -> ApiException.notFound("Заявка не найдена"));
        Event e = eventService.requireOrganized(p.getEventId(), me);
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

    private boolean isFull(Event e) {
        return e.getCapacity() != null && mapper.count(e.getId(), ParticipationStatus.CONFIRMED) >= e.getCapacity();
    }

    private void promoteFromWaitlist(Event e) {
        if (e.getStatus() != EventStatus.PUBLISHED || e.isPast()) {
            return;
        }
        participations.findByEventId(e.getId()).stream()
                .filter(p -> p.getStatus() == ParticipationStatus.WAITLISTED)
                .min(Comparator.comparing(Participation::getCreatedAt))
                .ifPresent(p -> {
                    if (e.getJoinMode() == JoinMode.OPEN) {
                        p.setStatus(ParticipationStatus.CONFIRMED);
                        p.setDecidedAt(Instant.now());
                        notificationService.notify(p.getUserId(), NotificationType.PARTICIPATION_APPROVED,
                                Map.of("event_id", e.getId(), "event_title", e.getTitle()));
                    } else {
                        p.setStatus(ParticipationStatus.PENDING);
                        notificationService.notify(e.getOrganizerUserId(), NotificationType.PARTICIPATION_REQUESTED,
                                Map.of("event_id", e.getId(), "event_title", e.getTitle(), "participation_id",
                                        p.getId()));
                    }
                    participations.save(p);
                });
    }
}
