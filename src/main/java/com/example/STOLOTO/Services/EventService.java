package com.example.STOLOTO.Services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.admin.AdminEventDetailsResponse;
import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.DTO.event.CreateEventRequest;
import com.example.STOLOTO.DTO.event.EventDetailsResponse;
import com.example.STOLOTO.DTO.event.EventShortResponse;
import com.example.STOLOTO.DTO.event.UpdateEventRequest;
import com.example.STOLOTO.Models.AuditAction;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.JoinMode;
import com.example.STOLOTO.Models.Level;
import com.example.STOLOTO.Models.NotificationType;
import com.example.STOLOTO.Models.ParticipationStatus;
import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.repository.CommentRepository;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.ParticipationRepository;
import com.example.STOLOTO.security.CurrentUser;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class EventService {

    private final EventRepository events;
    private final ParticipationRepository participations;
    private final CommentRepository comments;
    private final SportService sportService;
    private final OrganizationService organizationService;
    private final NotificationService notificationService;
    private final AuditService audit;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    /** Фильтры ленты и админского поиска. organizer — id пользователя, organization — id организации. */
    public record Filter(String q, Long sport, String city, LocalDate dateFrom, LocalDate dateTo, Level level,
            JoinMode joinMode, Long organizer, Long organization) {
    }

    // ---------- лента / карточка

    /** Только published и starts_at > now, сортировка по дате. */
    @Transactional(readOnly = true)
    public PageResponse<EventShortResponse> search(Filter f, Integer page, Integer size) {
        var pageable = PageResponse.pageable(page, size, Sort.by("startsAt"));
        return PageResponse.of(events.findAll(spec(f, EventStatus.PUBLISHED, true), pageable), mapper::toEventShort);
    }

    @Transactional(readOnly = true)
    public EventDetailsResponse get(Long id) {
        Event e = requireViewable(id);
        return mapper.toEventDetails(e, currentUser.find().orElse(null));
    }

    /**
     * Опубликованное событие видно всем. Снятое/удалённое/отменённое — админу, организатору
     * и тем, кто записывался (с баннером-причиной); остальным — 410.
     */
    @Transactional(readOnly = true)
    public Event requireViewable(Long id) {
        Event e = require(id);
        if (e.getStatus() == EventStatus.PUBLISHED) {
            return e;
        }
        User viewer = currentUser.find().orElse(null);
        if (viewer != null && (viewer.getRole() == Role.ADMIN || mapper.isOrganizer(e, viewer.getId())
                || participations.findByEventIdAndUserId(id, viewer.getId()).isPresent())) {
            return e;
        }
        throw unavailable(e);
    }

    // ---------- организатор

    public EventDetailsResponse create(CreateEventRequest r) {
        User me = currentUser.require();
        Event e = new Event();
        // Организатор — ровно один: пользователь ЛИБО организация (chk_one_organizer)
        if (r.organizationId() != null) {
            organizationService.requireApprovedOwned(r.organizationId(), me.getId());
            e.setOrganizerOrgId(r.organizationId());
        } else {
            e.setOrganizerUserId(me.getId());
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
        e.setUpdatedAt(e.getCreatedAt());
        return mapper.toEventDetails(events.save(e), me);
    }

    public EventDetailsResponse update(Long id, UpdateEventRequest r) {
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
        notifyParticipants(e, NotificationType.EVENT_UPDATED, "Событие изменено",
                "Организатор изменил «" + e.getTitle() + "». Проверьте детали.");
        return mapper.toEventDetails(e, me);
    }

    public EventDetailsResponse cancel(Long id, String reason) {
        User me = currentUser.require();
        Event e = requireOrganized(id, me);
        if (e.getStatus() != EventStatus.PUBLISHED) {
            throw ApiException.conflict("INVALID_STATUS", "Отменить можно только опубликованное событие");
        }
        e.setStatus(EventStatus.CANCELLED);
        e.setCancelReason(reason);
        notifyParticipants(e, NotificationType.EVENT_CANCELLED, "Событие отменено",
                "«" + e.getTitle() + "» отменено организатором. Причина: " + reason);
        return mapper.toEventDetails(e, me);
    }

    // ---------- админ

    @Transactional(readOnly = true)
    public PageResponse<EventShortResponse> adminSearch(EventStatus status, String q, Long organizer,
            Long organization, Integer page, Integer size) {
        Filter f = new Filter(q, null, null, null, null, null, null, organizer, organization);
        var pageable = PageResponse.pageable(page, size, Sort.by(Sort.Direction.DESC, "startsAt"));
        return PageResponse.of(events.findAll(spec(f, status, false), pageable), mapper::toEventShort);
    }

    @Transactional(readOnly = true)
    public AdminEventDetailsResponse adminDetails(Long id) {
        User admin = currentUser.require();
        Event e = require(id);
        var allParticipations = participations.findByEventIdOrderByCreatedAtAsc(id).stream()
                .map(p -> mapper.toParticipant(p, true))
                .toList();
        var eventComments = comments.findByEventIdOrderByCreatedAtAsc(id).stream()
                .map(c -> mapper.toComment(c, e, true))
                .toList();
        return new AdminEventDetailsResponse(mapper.toEventDetails(e, admin), mapper.userShort(e.getModeratedBy()),
                e.getModeratedAt(), allParticipations, eventComments);
    }

    public EventDetailsResponse unpublish(Long id, String reason) {
        Event e = require(id);
        if (e.getStatus() != EventStatus.PUBLISHED) {
            throw ApiException.conflict("INVALID_STATUS", "Снять можно только опубликованное событие");
        }
        return moderate(e, EventStatus.UNPUBLISHED, AuditAction.EVENT_UNPUBLISHED,
                NotificationType.EVENT_UNPUBLISHED, "Событие снято с публикации", reason);
    }

    public EventDetailsResponse delete(Long id, String reason) {
        Event e = require(id);
        if (e.getStatus() == EventStatus.DELETED) {
            throw ApiException.conflict("INVALID_STATUS", "Событие уже удалено");
        }
        return moderate(e, EventStatus.DELETED, AuditAction.EVENT_DELETED, NotificationType.EVENT_DELETED,
                "Событие удалено администратором", reason);
    }

    public EventDetailsResponse restore(Long id) {
        Event e = require(id);
        if (e.getStatus() != EventStatus.UNPUBLISHED && e.getStatus() != EventStatus.DELETED) {
            throw ApiException.conflict("INVALID_STATUS", "Восстановить можно только снятое или удалённое событие");
        }
        return moderate(e, EventStatus.PUBLISHED, AuditAction.EVENT_RESTORED, NotificationType.EVENT_RESTORED,
                "Публикация восстановлена", null);
    }

    // ---------- helpers

    public Event require(Long id) {
        return events.findById(id).orElseThrow(() -> ApiException.notFound("Мероприятие не найдено"));
    }

    /** Событие, где текущий пользователь — организатор (админ тоже получает 403). */
    public Event requireOrganized(Long id, User me) {
        Event e = require(id);
        if (!mapper.isOrganizer(e, me.getId())) {
            throw ApiException.forbidden("Вы не организатор этого мероприятия");
        }
        return e;
    }

    public static ApiException unavailable(Event e) {
        String reason = e.unavailableReason();
        String status = e.getStatus().name().toLowerCase();
        return ApiException.gone("Мероприятие недоступно" + (reason == null ? "" : ": " + reason),
                reason == null ? Map.of("status", status) : Map.of("status", status, "reason", reason));
    }

    private EventDetailsResponse moderate(Event e, EventStatus status, AuditAction action,
            NotificationType notification, String title, String reason) {
        User admin = currentUser.require();
        e.setStatus(status);
        e.setModerationReason(reason);
        e.setModeratedBy(admin.getId());
        e.setModeratedAt(Instant.now());
        audit.event(admin.getId(), e.getId(), action, reason);
        notificationService.notify(mapper.organizerRecipient(e), notification, title,
                "«" + e.getTitle() + "»" + (reason == null ? "" : ". Причина: " + reason), e.getId());
        return mapper.toEventDetails(e, admin);
    }

    private void notifyParticipants(Event e, NotificationType type, String title, String body) {
        participations.findByEventIdAndStatusInOrderByCreatedAtAsc(e.getId(), ParticipationStatus.ACTIVE)
                .forEach(p -> notificationService.notify(p.getUserId(), type, title, body, e.getId()));
    }

    private Long resolveSport(Long sportId, String sportName, Long userId) {
        if (sportId != null) {
            return sportService.validateIds(List.of(sportId), "sport_id").getFirst();
        }
        if (sportName != null && !sportName.isBlank()) {
            return sportService.findOrCreate(sportName, null, userId).getId();
        }
        throw ApiException.validation("sport_id", "Укажите sport_id или sport_name");
    }

    private static Specification<Event> spec(Filter f, EventStatus status, boolean upcomingOnly) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (status != null) {
                p.add(cb.equal(root.get("status"), status));
            }
            if (upcomingOnly) {
                p.add(cb.greaterThan(root.get("startsAt"), Instant.now()));
            }
            if (f.q() != null && !f.q().isBlank()) {
                String like = "%" + f.q().trim().toLowerCase() + "%";
                p.add(cb.or(cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(cb.coalesce(root.get("description"), "")), like)));
            }
            if (f.sport() != null) {
                p.add(cb.equal(root.get("sportId"), f.sport()));
            }
            if (f.city() != null && !f.city().isBlank()) {
                String city = f.city().trim().toLowerCase();
                p.add(cb.or(cb.equal(cb.lower(root.get("city")), city),
                        cb.like(cb.lower(root.get("address")), "%" + city + "%")));
            }
            ZoneId zone = ZoneId.systemDefault();
            if (f.dateFrom() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("startsAt"), f.dateFrom().atStartOfDay(zone).toInstant()));
            }
            if (f.dateTo() != null) {
                p.add(cb.lessThan(root.get("startsAt"), f.dateTo().plusDays(1).atStartOfDay(zone).toInstant()));
            }
            if (f.level() != null) {
                p.add(cb.equal(root.get("level"), f.level()));
            }
            if (f.joinMode() != null) {
                p.add(cb.equal(root.get("joinMode"), f.joinMode()));
            }
            if (f.organizer() != null) {
                p.add(cb.equal(root.get("organizerUserId"), f.organizer()));
            }
            if (f.organization() != null) {
                p.add(cb.equal(root.get("organizerOrgId"), f.organization()));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}
