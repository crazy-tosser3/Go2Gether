package com.example.STOLOTO.Services;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.example.STOLOTO.DTO.admin.ModerationLogResponse;
import com.example.STOLOTO.DTO.comment.CommentResponse;
import com.example.STOLOTO.DTO.event.EventCard;
import com.example.STOLOTO.DTO.event.EventDetailsResponse;
import com.example.STOLOTO.DTO.event.EventShortResponse;
import com.example.STOLOTO.DTO.event.OrganizerResponse;
import com.example.STOLOTO.DTO.notification.NotificationResponse;
import com.example.STOLOTO.DTO.organization.OrganizationResponse;
import com.example.STOLOTO.DTO.organization.OrganizationShortResponse;
import com.example.STOLOTO.DTO.participation.ParticipantResponse;
import com.example.STOLOTO.DTO.participation.ParticipationResponse;
import com.example.STOLOTO.DTO.sport.SportResponse;
import com.example.STOLOTO.DTO.user.UserResponse;
import com.example.STOLOTO.DTO.user.UserShortResponse;
import com.example.STOLOTO.Models.AuditLog;
import com.example.STOLOTO.Models.Comment;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.Notification;
import com.example.STOLOTO.Models.Organization;
import com.example.STOLOTO.Models.OrganizerType;
import com.example.STOLOTO.Models.Participation;
import com.example.STOLOTO.Models.ParticipationStatus;
import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.Sport;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.ParticipationRepository;
import com.example.STOLOTO.repository.SportRepository;
import com.example.STOLOTO.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/** Сборка DTO из сущностей + общая проверка «кто организатор события». */
@Component
@RequiredArgsConstructor
public class DtoMapper {

    private static final UserShortResponse DELETED_USER = new UserShortResponse(null, "Удалённый пользователь", null);

    private final UserRepository users;
    private final SportRepository sports;
    private final OrganizationRepository organizations;
    private final ParticipationRepository participations;

    // ---------- users

    public UserResponse toUser(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getAvatarUrl(), u.getCity(),
                u.getAbout(), u.getLevel(), List.copyOf(u.getSportIds()), u.isDemo(), u.getCreatedAt());
    }

    public UserShortResponse toUserShort(User u) {
        return new UserShortResponse(u.getId(), u.getName(), u.getAvatarUrl());
    }

    public UserShortResponse userShort(Long userId) {
        return userId == null ? null : users.findById(userId).map(this::toUserShort).orElse(DELETED_USER);
    }

    // ---------- sports

    public SportResponse toSport(Sport s) {
        return new SportResponse(s.getId(), s.getName(), s.getCategory(), s.getIcon(), s.isCustom());
    }

    public SportResponse sport(Long sportId) {
        return sportId == null ? null : sports.findById(sportId).map(this::toSport).orElse(null);
    }

    public List<SportResponse> sports(Collection<Long> sportIds) {
        return sportIds == null ? List.of() : sportIds.stream().map(this::sport).filter(Objects::nonNull).toList();
    }

    // ---------- organizations

    public OrganizationShortResponse toOrganizationShort(Organization o) {
        return new OrganizationShortResponse(o.getId(), o.getName(), o.getType(), o.getCity(), o.getStatus());
    }

    public OrganizationShortResponse organizationShort(Long orgId) {
        return orgId == null ? null : organizations.findById(orgId).map(this::toOrganizationShort).orElse(null);
    }

    /** privileged — владелец или админ: видит причину отклонения и дату решения. */
    public OrganizationResponse toOrganization(Organization o, boolean privileged) {
        return new OrganizationResponse(o.getId(), o.getName(), o.getDescription(), o.getType(),
                o.getSportsDescription(), o.getCity(), o.getWebsiteUrl(), o.getAvatarUrl(), o.getContacts(),
                o.getStatus(), privileged ? o.getRejectReason() : null, privileged ? o.getDecidedAt() : null,
                userShort(o.getOwnerId()), o.getCreatedAt(), o.getUpdatedAt());
    }

    // ---------- events

    /** Организатор события — автор-пользователь или владелец организации-организатора. */
    public boolean isOrganizer(Event e, Long userId) {
        if (userId == null) {
            return false;
        }
        if (userId.equals(e.getOrganizerUserId())) {
            return true;
        }
        return e.getOrganizerOrgId() != null && organizations.findById(e.getOrganizerOrgId())
                .map(o -> userId.equals(o.getOwnerId())).orElse(false);
    }

    /** Кому слать уведомления организатора: автору или владельцу организации. */
    public Long organizerRecipient(Event e) {
        if (e.getOrganizerUserId() != null) {
            return e.getOrganizerUserId();
        }
        return organizations.findById(e.getOrganizerOrgId()).map(Organization::getOwnerId).orElse(null);
    }

    public OrganizerResponse organizer(Event e) {
        if (e.organizerType() == OrganizerType.ORGANIZATION) {
            var org = organizations.findById(e.getOrganizerOrgId());
            return new OrganizerResponse(OrganizerType.ORGANIZATION, e.getOrganizerOrgId(),
                    org.map(Organization::getName).orElse(null), org.map(Organization::getAvatarUrl).orElse(null));
        }
        UserShortResponse u = userShort(e.getOrganizerUserId());
        return new OrganizerResponse(OrganizerType.USER, e.getOrganizerUserId(), u.name(), u.avatarUrl());
    }

    public String organizerContacts(Event e) {
        if (e.organizerType() == OrganizerType.ORGANIZATION) {
            return organizations.findById(e.getOrganizerOrgId()).map(Organization::getContacts).orElse(null);
        }
        return users.findById(e.getOrganizerUserId()).map(User::getEmail).orElse(null);
    }

    public long count(Long eventId, ParticipationStatus status) {
        return participations.countByEventIdAndStatus(eventId, status);
    }

    public EventShortResponse toEventShort(Event e) {
        return new EventShortResponse(e.getId(), e.getTitle(), sport(e.getSportId()), organizer(e), e.getStartsAt(),
                e.getEndsAt(), e.getCity(), e.getAddress(), e.getLevel(), e.getJoinMode(), e.getCapacity(),
                count(e.getId(), ParticipationStatus.CONFIRMED), e.getStatus(), e.isPast(), e.isEdited());
    }

    /** Карточка события глазами viewer (null — гость). */
    public EventDetailsResponse toEventDetails(Event e, User viewer) {
        return personalize(toEventCard(e), viewer);
    }

    /** Общая для всех зрителей карточка (кладётся в кэш): полная информация, без «моей записи». */
    public EventCard toEventCard(Event e) {
        var confirmed =
                participations.findByEventIdAndStatusOrderByCreatedAtAsc(e.getId(), ParticipationStatus.CONFIRMED);
        var full = new EventDetailsResponse(e.getId(), e.getTitle(), e.getDescription(), sport(e.getSportId()),
                organizer(e), organizerContacts(e), e.getStartsAt(), e.getEndsAt(), e.getCity(), e.getAddress(),
                e.getLocationNote(), e.getLat(), e.getLng(), e.getLevel(), e.getRequirements(), e.getEquipment(),
                e.getJoinMode(), e.getCapacity(), confirmed.size(),
                confirmed.stream().map(p -> userShort(p.getUserId())).toList(),
                e.getStatus(), e.getStatus() == EventStatus.PUBLISHED ? null : e.unavailableReason(),
                e.isPast(), e.isEdited(), false, null, e.getCreatedAt(), e.getUpdatedAt());
        Long ownerId = e.getOrganizerOrgId() == null
                ? null
                : organizations.findById(e.getOrganizerOrgId()).map(Organization::getOwnerId).orElse(null);
        return new EventCard(full, e.getOrganizerUserId(), ownerId);
    }

    /** Наложить на общую карточку данные конкретного зрителя (1 запрос — его запись на событие). */
    public EventDetailsResponse personalize(EventCard card, User viewer) {
        Long viewerId = viewer == null ? null : viewer.getId();
        boolean organizer = card.isOrganizer(viewerId);
        boolean admin = viewer != null && viewer.getRole() == Role.ADMIN;
        Participation mine = viewerId == null
                ? null
                : participations.findByEventIdAndUserId(card.full().id(), viewerId).orElse(null);
        boolean confirmedParticipant = mine != null && mine.getStatus() == ParticipationStatus.CONFIRMED;
        return card.full().forViewer(organizer || admin || confirmedParticipant, organizer,
                mine == null ? null : toParticipation(mine));
    }

    // ---------- participations

    public ParticipationResponse toParticipation(Participation p) {
        return new ParticipationResponse(p.getId(), p.getEventId(), p.getUserId(), p.getStatus(), p.getMessage(),
                p.getDecisionReason(), p.getDecidedAt(), p.getCreatedAt());
    }

    public ParticipantResponse toParticipant(Participation p, boolean withContacts) {
        String email = withContacts ? users.findById(p.getUserId()).map(User::getEmail).orElse(null) : null;
        return new ParticipantResponse(p.getId(), userShort(p.getUserId()), email, p.getStatus(),
                withContacts ? p.getMessage() : null, withContacts ? p.getDecisionReason() : null, p.getCreatedAt());
    }

    // ---------- comments

    public CommentResponse toComment(Comment c, Event e, boolean admin) {
        boolean byOrganizer = c.getAuthorId().equals(e.getOrganizerUserId())
                || (c.getAsOrganizationId() != null && c.getAsOrganizationId().equals(e.getOrganizerOrgId()));
        return new CommentResponse(c.getId(), c.getEventId(), c.getParentId(), userShort(c.getAuthorId()),
                organizationShort(c.getAsOrganizationId()), byOrganizer, c.isHidden() ? null : c.getText(),
                c.isHidden(), admin ? c.getHiddenReason() : null, c.getUpdatedAt() != null, c.getCreatedAt(),
                c.getUpdatedAt());
    }

    // ---------- misc

    public NotificationResponse toNotification(Notification n) {
        return new NotificationResponse(n.getId(), n.getType(), n.getTitle(), n.getBody(), n.getEventId(),
                n.isRead(), n.getCreatedAt());
    }

    public ModerationLogResponse toModerationLog(AuditLog l) {
        return new ModerationLogResponse(l.getId(), userShort(l.getAdminId()), l.getAction(), l.targetType(),
                l.targetId(), l.getReason(), l.getCreatedAt());
    }
}
