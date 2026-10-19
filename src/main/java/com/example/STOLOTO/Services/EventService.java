package com.example.STOLOTO.Services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.STOLOTO.DTO.admin.AdminEventDetailsResponse;
import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.DTO.event.CreateEventRequest;
import com.example.STOLOTO.DTO.event.EventDetailsResponse;
import com.example.STOLOTO.DTO.event.EventShortResponse;
import com.example.STOLOTO.DTO.event.UpdateEventRequest;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.Models.Comment;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.JoinMode;
import com.example.STOLOTO.Models.Level;
import com.example.STOLOTO.Models.ModerationAction;
import com.example.STOLOTO.Models.ModerationTargetType;
import com.example.STOLOTO.Models.NotificationType;
import com.example.STOLOTO.Models.OrganizerType;
import com.example.STOLOTO.Models.Participation;
import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.repository.CommentRepository;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.ParticipationRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository events;
    private final ParticipationRepository participations;
    private final CommentRepository comments;
    private final SportService sportService;
    private final OrganizationService organizationService;
    private final NotificationService notificationService;
    private final ModerationLogService moderationLog;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    public record Filter(String q, String sport, String city, LocalDate dateFrom, LocalDate dateTo, Level level,
            JoinMode joinMode, String organizer) {
    }

    // лента / карточка

    public PageResponse<EventShortResponse> search(Filter f, Integer page, Integer size) {
        var items = events.findByStatus(EventStatus.PUBLISHED).stream()
                .filter(e -> !e.isPast())
                .filter(e -> matches(e, f))
                .sorted(Comparator.comparing(Event::getStartsAt))
                .map(mapper::toEventShort)
                .toList();
        return PageResponse.of(items, page, size);
    }

    public EventDetailsResponse get(String id) {
        Event e = requireViewable(id);
        return mapper.toEventDetails(e, currentUser.find().orElse(null));
    }

    public Event requireViewable(String id) {
        Event e = require(id);
        if (e.getStatus() == EventStatus.PUBLISHED) {
            return e;
        }
        User viewer = currentUser.find().orElse(null);
        if (viewer != null && (viewer.getRole() == Role.ADMIN || mapper.isOrganizer(e, viewer.getId())
                || !participations.findByEventIdAndUserId(id, viewer.getId()).isEmpty())) {
            return e;
        }
        throw unavailable(e);
    }

    // организатор

    public EventDetailsResponse create(CreateEventRequest r) {
        User me = currentUser.require();
        Event e = new Event();
        e.setId(UUID.randomUUID().toString());
        e.setOrganizerUserId(me.getId());
        if (r.organizationId() != null && !r.organizationId().isBlank()) {
            organizationService.requireApprovedOwned(r.organizationId(), me.getId());
            e.setOrganizerType(OrganizerType.ORGANIZATION);
            e.setOrganizerOrgId(r.organizationId());
        } else {
            e.setOrganizerType(OrganizerType.USER);
        }
        e.setSportId(resolveSport(r.sportId(), r.sportName(), me.getId()));
        if (r.endsAt() != null && !r.endsAt().isAfter(r.startsAt())) {
            throw ApiException.validation("ends_at", "должно быть позже starts_at");
        }
        e.setTitle(r.title().trim());
        e.setDescription(r.description());
        e.setStartsAt(r.startsAt());
        e.setEndsAt(r.endsAt());
        e.setCity(UserService.blankToNull(r.city()));
        e.setAddress(r.address().trim());
        e.setLocationNote(r.locationNote());
        e.setLat(r.lat());
        e.setLng(r.lng());
        e.setLevel(r.level());
        e.setRequirements(r.requirements());
        e.setEquipment(r.equipment());
        e.setJoinMode(r.joinMode());
        e.setCapacity(r.capacity());
        e.setStatus(EventStatus.PUBLISHED);
        e.setCreatedAt(Instant.now());
        return mapper.toEventDetails(events.save(e), me);
    }

    public EventDetailsResponse update(String id, UpdateEventRequest r) {
        User me = currentUser.require();
        Event e = requireOrganized(id, me);
        if (e.getStatus() != EventStatus.PUBLISHED) {
            throw unavailable(e);
        }
        if (r.sportId() != null || r.sportName() != null) {
            e.setSportId(resolveSport(r.sportId(), r.sportName(), me.getId()));
        }
        if (r.title() != null) {
            e.setTitle(r.title().trim());
        }
        if (r.description() != null) {
            e.setDescription(r.description());
        }
        if (r.startsAt() != null) {
            e.setStartsAt(r.startsAt());
        }
        if (r.endsAt() != null) {
            e.setEndsAt(r.endsAt());
        }
        if (e.getEndsAt() != null && !e.getEndsAt().isAfter(e.getStartsAt())) {
            throw ApiException.validation("ends_at", "должно быть позже starts_at");
        }
        if (r.city() != null) {
            e.setCity(UserService.blankToNull(r.city()));
        }
        if (r.address() != null) {
            e.setAddress(r.address().trim());
        }
        if (r.locationNote() != null) {
            e.setLocationNote(r.locationNote());
        }
        if (r.lat() != null) {
            e.setLat(r.lat());
        }
        if (r.lng() != null) {
            e.setLng(r.lng());
        }
        if (r.level() != null) {
            e.setLevel(r.level());
        }
        if (r.requirements() != null) {
            e.setRequirements(r.requirements());
        }
        if (r.equipment() != null) {
            e.setEquipment(r.equipment());
        }
        if (r.joinMode() != null) {
            e.setJoinMode(r.joinMode());
        }
        if (r.capacity() != null) {
            e.setCapacity(r.capacity());
        }
        e.setUpdatedAt(Instant.now());
        events.save(e);
        notifyParticipants(e, NotificationType.EVENT_UPDATED, null);
        return mapper.toEventDetails(e, me);
    }

    public EventDetailsResponse cancel(String id, String reason) {
        User me = currentUser.require();
        Event e = requireOrganized(id, me);
        if (e.getStatus() != EventStatus.PUBLISHED) {
            throw ApiException.conflict("INVALID_STATUS", "Отменить можно только опубликованное событие");
        }
        e.setStatus(EventStatus.CANCELLED);
        e.setCancelReason(reason);
        events.save(e);
        notifyParticipants(e, NotificationType.EVENT_CANCELLED, reason);
        return mapper.toEventDetails(e, me);
    }

    // админ

    public PageResponse<EventShortResponse> adminSearch(EventStatus status, String q, String organizer,
            Integer page, Integer size) {
        Filter f = new Filter(q, null, null, null, null, null, null, organizer);
        var items = events.findAll().stream()
                .filter(e -> status == null || e.getStatus() == status)
                .filter(e -> matches(e, f))
                .sorted(Comparator.comparing(Event::getStartsAt).reversed())
                .map(mapper::toEventShort)
                .toList();
        return PageResponse.of(items, page, size);
    }

    public AdminEventDetailsResponse adminDetails(String id) {
        User admin = currentUser.require();
        Event e = require(id);
        var allParticipations = participations.findByEventId(id).stream()
                .sorted(Comparator.comparing(Participation::getCreatedAt))
                .map(p -> mapper.toParticipant(p, true))
                .toList();
        var eventComments = comments.findByEventId(id).stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(c -> mapper.toComment(c, e, true))
                .toList();
        return new AdminEventDetailsResponse(mapper.toEventDetails(e, admin), mapper.userShort(e.getModeratedBy()),
                e.getModeratedAt(), allParticipations, eventComments);
    }

    public EventDetailsResponse unpublish(String id, String reason) {
        Event e = require(id);
        if (e.getStatus() != EventStatus.PUBLISHED) {
            throw ApiException.conflict("INVALID_STATUS", "Снять можно только опубликованное событие");
        }
        return moderate(e, EventStatus.UNPUBLISHED, ModerationAction.UNPUBLISH, NotificationType.EVENT_UNPUBLISHED,
                reason);
    }

    public EventDetailsResponse delete(String id, String reason) {
        Event e = require(id);
        if (e.getStatus() == EventStatus.DELETED) {
            throw ApiException.conflict("INVALID_STATUS", "Событие уже удалено");
        }
        return moderate(e, EventStatus.DELETED, ModerationAction.DELETE, NotificationType.EVENT_DELETED, reason);
    }

    public EventDetailsResponse restore(String id) {
        Event e = require(id);
        if (e.getStatus() != EventStatus.UNPUBLISHED && e.getStatus() != EventStatus.DELETED) {
            throw ApiException.conflict("INVALID_STATUS", "Восстановить можно только снятое или удалённое событие");
        }
        return moderate(e, EventStatus.PUBLISHED, ModerationAction.RESTORE, NotificationType.EVENT_RESTORED, null);
    }

    // helpers

    public Event require(String id) {
        return events.findById(id).orElseThrow(() -> ApiException.notFound("Мероприятие не найдено"));
    }

    public Event requireOrganized(String id, User me) {
        Event e = require(id);
        if (!mapper.isOrganizer(e, me.getId())) {
            throw ApiException.forbidden("Вы не организатор этого мероприятия");
        }
        return e;
    }

    public static ApiException unavailable(Event e) {
        String reason = e.unavailableReason();
        return ApiException.gone("Мероприятие недоступно" + (reason == null ? "" : ": " + reason),
                reason == null
                        ? Map.of("status", e.getStatus().name().toLowerCase())
                        : Map.of("status", e.getStatus().name().toLowerCase(), "reason", reason));
    }

    private EventDetailsResponse moderate(Event e, EventStatus status, ModerationAction action,
            NotificationType notification, String reason) {
        User admin = currentUser.require();
        e.setStatus(status);
        e.setModerationReason(reason);
        e.setModeratedBy(admin.getId());
        e.setModeratedAt(Instant.now());
        events.save(e);
        moderationLog.log(admin.getId(), ModerationTargetType.EVENT, e.getId(), action, reason);
        notificationService.notify(e.getOrganizerUserId(), notification, payload(e, reason));
        return mapper.toEventDetails(e, admin);
    }

    private void notifyParticipants(Event e, NotificationType type, String reason) {
        participations.findByEventId(e.getId()).stream()
                .filter(p -> p.getStatus().isActive())
                .forEach(p -> notificationService.notify(p.getUserId(), type, payload(e, reason)));
    }

    private static Map<String, String> payload(Event e, String reason) {
        return reason == null
                ? Map.of("event_id", e.getId(), "event_title", e.getTitle())
                : Map.of("event_id", e.getId(), "event_title", e.getTitle(), "reason", reason);
    }

    private String resolveSport(String sportId, String sportName, String userId) {
        if (sportId != null && !sportId.isBlank()) {
            return sportService.validateIds(List.of(sportId), "sport_id").getFirst();
        }
        if (sportName != null && !sportName.isBlank()) {
            return sportService.findOrCreate(sportName, null, userId).getId();
        }
        throw ApiException.validation("sport_id", "Укажите sport_id или sport_name");
    }

    private boolean matches(Event e, Filter f) {
        if (f.q() != null && !f.q().isBlank()) {
            String needle = f.q().trim().toLowerCase();
            boolean hit = e.getTitle().toLowerCase().contains(needle)
                    || (e.getDescription() != null && e.getDescription().toLowerCase().contains(needle));
            if (!hit) {
                return false;
            }
        }
        if (f.sport() != null && !f.sport().isBlank() && !f.sport().equals(e.getSportId())) {
            return false;
        }
        if (f.city() != null && !f.city().isBlank()) {
            String city = f.city().trim().toLowerCase();
            boolean hit = (e.getCity() != null && e.getCity().equalsIgnoreCase(f.city().trim()))
                    || (e.getAddress() != null && e.getAddress().toLowerCase().contains(city));
            if (!hit) {
                return false;
            }
        }
        ZoneId zone = ZoneId.systemDefault();
        if (f.dateFrom() != null && e.getStartsAt().isBefore(f.dateFrom().atStartOfDay(zone).toInstant())) {
            return false;
        }
        if (f.dateTo() != null && !e.getStartsAt().isBefore(f.dateTo().plusDays(1).atStartOfDay(zone).toInstant())) {
            return false;
        }
        if (f.level() != null && f.level() != e.getLevel()) {
            return false;
        }
        if (f.joinMode() != null && f.joinMode() != e.getJoinMode()) {
            return false;
        }
        if (f.organizer() != null && !f.organizer().isBlank()) {
            return f.organizer().equals(e.getOrganizerUserId()) || f.organizer().equals(e.getOrganizerOrgId());
        }
        return true;
    }
}
