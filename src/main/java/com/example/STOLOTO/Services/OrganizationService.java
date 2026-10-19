package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.STOLOTO.DTO.admin.AdminOrganizationDetailsResponse;
import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.DTO.event.EventShortResponse;
import com.example.STOLOTO.DTO.organization.CreateOrganizationRequest;
import com.example.STOLOTO.DTO.organization.OrganizationResponse;
import com.example.STOLOTO.DTO.organization.UpdateOrganizationRequest;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.ModerationAction;
import com.example.STOLOTO.Models.ModerationTargetType;
import com.example.STOLOTO.Models.NotificationType;
import com.example.STOLOTO.Models.Organization;
import com.example.STOLOTO.Models.OrganizationStatus;
import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.UserRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizations;
    private final EventRepository events;
    private final UserRepository users;
    private final SportService sportService;
    private final NotificationService notificationService;
    private final ModerationLogService moderationLog;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    // владелец

    public OrganizationResponse create(CreateOrganizationRequest r) {
        User me = currentUser.require();
        if (!organizations.findByOwnerId(me.getId()).isEmpty()) {
            throw ApiException.conflict("ORGANIZATION_EXISTS", "У вас уже есть организация");
        }
        Organization o = new Organization();
        o.setId(UUID.randomUUID().toString());
        o.setOwnerId(me.getId());
        o.setName(r.name().trim());
        o.setDescription(r.description());
        o.setType(r.type());
        o.setSportIds(r.sportIds() == null ? List.of() : sportService.validateIds(r.sportIds(), "sport_ids"));
        o.setCity(r.city());
        o.setContacts(r.contacts());
        o.setStatus(OrganizationStatus.PENDING);
        o.setCreatedAt(Instant.now());
        return mapper.toOrganization(organizations.save(o), true);
    }

    public OrganizationResponse my() {
        String me = currentUser.require().getId();
        return organizations.findByOwnerId(me).stream().findFirst()
                .map(o -> mapper.toOrganization(o, true))
                .orElseThrow(() -> ApiException.notFound("У вас нет организации"));
    }

    public OrganizationResponse update(String id, UpdateOrganizationRequest r) {
        Organization o = requireOwned(id);
        if (o.getStatus() == OrganizationStatus.BLOCKED) {
            throw ApiException.forbidden("Организация заблокирована");
        }
        if (r.name() != null) {
            o.setName(r.name().trim());
        }
        if (r.description() != null) {
            o.setDescription(r.description());
        }
        if (r.type() != null) {
            o.setType(r.type());
        }
        if (r.sportIds() != null) {
            o.setSportIds(sportService.validateIds(r.sportIds(), "sport_ids"));
        }
        if (r.city() != null) {
            o.setCity(r.city());
        }
        if (r.contacts() != null) {
            o.setContacts(r.contacts());
        }
        o.setUpdatedAt(Instant.now());
        return mapper.toOrganization(organizations.save(o), true);
    }

    public OrganizationResponse resubmit(String id) {
        Organization o = requireOwned(id);
        if (o.getStatus() != OrganizationStatus.REJECTED) {
            throw ApiException.conflict("INVALID_STATUS", "Повторно подать можно только отклонённую заявку");
        }
        o.setStatus(OrganizationStatus.PENDING);
        o.setRejectReason(null);
        o.setUpdatedAt(Instant.now());
        return mapper.toOrganization(organizations.save(o), true);
    }

    // публичное

    public PageResponse<OrganizationResponse> catalog(String q, String sport, String city, Integer page, Integer size) {
        var items = organizations.findByStatus(OrganizationStatus.APPROVED).stream()
                .filter(o -> matches(o, q))
                .filter(o -> sport == null || sport.isBlank() || o.getSportIds().contains(sport))
                .filter(o -> city == null || city.isBlank() || city.equalsIgnoreCase(o.getCity()))
                .sorted(Comparator.comparing(Organization::getName, String.CASE_INSENSITIVE_ORDER))
                .map(o -> mapper.toOrganization(o, false))
                .toList();
        return PageResponse.of(items, page, size);
    }

    public OrganizationResponse get(String id) {
        Organization o = requireVisible(id);
        return mapper.toOrganization(o, isPrivileged(o));
    }

    public List<EventShortResponse> events(String id) {
        requireVisible(id);
        return events.findByOrganizerOrgId(id).stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .sorted(Comparator.comparing(Event::getStartsAt))
                .map(mapper::toEventShort)
                .toList();
    }

    public Organization requireApprovedOwned(String orgId, String userId) {
        Organization o = organizations.findById(orgId)
                .orElseThrow(() -> ApiException.validation("organization_id", "Организация не найдена"));
        if (!userId.equals(o.getOwnerId())) {
            throw ApiException.forbidden("Вы не владелец этой организации");
        }
        if (o.getStatus() != OrganizationStatus.APPROVED) {
            throw ApiException.forbidden("Организация ещё не допущена");
        }
        return o;
    }

    // админ

    public PageResponse<OrganizationResponse> adminList(OrganizationStatus status, String q, Integer page,
            Integer size) {
        var items = organizations.findAll().stream()
                .filter(o -> status == null || o.getStatus() == status)
                .filter(o -> matches(o, q))
                .sorted(Comparator.comparing(Organization::getCreatedAt).reversed())
                .map(o -> mapper.toOrganization(o, true))
                .toList();
        return PageResponse.of(items, page, size);
    }

    public AdminOrganizationDetailsResponse adminDetails(String id) {
        Organization o = require(id);
        var owner = users.findById(o.getOwnerId()).map(mapper::toUser).orElse(null);
        var orgEvents = events.findByOrganizerOrgId(id).stream()
                .sorted(Comparator.comparing(Event::getStartsAt).reversed())
                .map(mapper::toEventShort)
                .toList();
        return new AdminOrganizationDetailsResponse(mapper.toOrganization(o, true), owner, orgEvents);
    }

    public OrganizationResponse approve(String id) {
        User admin = currentUser.require();
        Organization o = requirePending(id);
        o.setStatus(OrganizationStatus.APPROVED);
        o.setRejectReason(null);
        o.setReviewedBy(admin.getId());
        o.setReviewedAt(Instant.now());
        organizations.save(o);
        moderationLog.log(admin.getId(), ModerationTargetType.ORGANIZATION, id, ModerationAction.APPROVE, null);
        notificationService.notify(o.getOwnerId(), NotificationType.ORGANIZATION_APPROVED,
                Map.of("organization_id", id, "organization_name", o.getName()));
        return mapper.toOrganization(o, true);
    }

    public OrganizationResponse reject(String id, String reason) {
        User admin = currentUser.require();
        Organization o = requirePending(id);
        o.setStatus(OrganizationStatus.REJECTED);
        o.setRejectReason(reason);
        o.setReviewedBy(admin.getId());
        o.setReviewedAt(Instant.now());
        organizations.save(o);
        moderationLog.log(admin.getId(), ModerationTargetType.ORGANIZATION, id, ModerationAction.REJECT, reason);
        notificationService.notify(o.getOwnerId(), NotificationType.ORGANIZATION_REJECTED,
                Map.of("organization_id", id, "organization_name", o.getName(), "reason", reason));
        return mapper.toOrganization(o, true);
    }

    // helpers

    private Organization require(String id) {
        return organizations.findById(id).orElseThrow(() -> ApiException.notFound("Организация не найдена"));
    }

    private Organization requirePending(String id) {
        Organization o = require(id);
        if (o.getStatus() != OrganizationStatus.PENDING) {
            throw ApiException.conflict("ALREADY_REVIEWED", "Заявка уже рассмотрена",
                    Map.of("status", o.getStatus().name().toLowerCase()));
        }
        return o;
    }

    private Organization requireOwned(String id) {
        String me = currentUser.require().getId();
        Organization o = require(id);
        if (!me.equals(o.getOwnerId())) {
            throw ApiException.forbidden("Вы не владелец этой организации");
        }
        return o;
    }

    private Organization requireVisible(String id) {
        Organization o = require(id);
        if (o.getStatus() != OrganizationStatus.APPROVED && !isPrivileged(o)) {
            throw ApiException.notFound("Организация не найдена");
        }
        return o;
    }

    private boolean isPrivileged(Organization o) {
        return currentUser.find()
                .map(u -> u.getRole() == Role.ADMIN || u.getId().equals(o.getOwnerId()))
                .orElse(false);
    }

    private static boolean matches(Organization o, String q) {
        if (q == null || q.isBlank()) {
            return true;
        }
        String needle = q.trim().toLowerCase();
        return o.getName().toLowerCase().contains(needle)
                || (o.getDescription() != null && o.getDescription().toLowerCase().contains(needle));
    }
}
