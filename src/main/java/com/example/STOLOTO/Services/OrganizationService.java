package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.admin.AdminOrganizationDetailsResponse;
import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.DTO.event.EventShortResponse;
import com.example.STOLOTO.DTO.organization.CreateOrganizationRequest;
import com.example.STOLOTO.DTO.organization.OrganizationResponse;
import com.example.STOLOTO.DTO.organization.UpdateOrganizationRequest;
import com.example.STOLOTO.Models.AuditAction;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.NotificationType;
import com.example.STOLOTO.Models.Organization;
import com.example.STOLOTO.Models.OrganizationStatus;
import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.UserRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizations;
    private final EventRepository events;
    private final UserRepository users;
    private final NotificationService notificationService;
    private final AuditService audit;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    // ---------- владелец (у одного владельца может быть несколько организаций)

    public OrganizationResponse create(CreateOrganizationRequest r) {
        User me = currentUser.require();
        Organization o = new Organization();
        o.setOwnerId(me.getId());
        o.setName(r.name().trim());
        o.setDescription(r.description());
        o.setType(r.type());
        o.setSportsDescription(r.sportsDescription());
        o.setCity(r.city());
        o.setWebsiteUrl(r.websiteUrl());
        o.setAvatarUrl(r.avatarUrl());
        o.setContacts(r.contacts());
        o.setStatus(OrganizationStatus.PENDING);
        o.setCreatedAt(Instant.now());
        o.setUpdatedAt(o.getCreatedAt());
        return mapper.toOrganization(organizations.save(o), true);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> my() {
        Long me = currentUser.require().getId();
        return organizations.findByOwnerIdOrderByCreatedAtDesc(me).stream()
                .map(o -> mapper.toOrganization(o, true))
                .toList();
    }

    public OrganizationResponse update(Long id, UpdateOrganizationRequest r) {
        Organization o = requireOwned(id);
        if (r.name() != null) {
            o.setName(r.name().trim());
        }
        if (r.description() != null) {
            o.setDescription(r.description());
        }
        if (r.type() != null) {
            o.setType(r.type());
        }
        if (r.sportsDescription() != null) {
            o.setSportsDescription(r.sportsDescription());
        }
        if (r.city() != null) {
            o.setCity(r.city());
        }
        if (r.websiteUrl() != null) {
            o.setWebsiteUrl(UserService.blankToNull(r.websiteUrl()));
        }
        if (r.avatarUrl() != null) {
            o.setAvatarUrl(UserService.blankToNull(r.avatarUrl()));
        }
        if (r.contacts() != null) {
            o.setContacts(r.contacts());
        }
        o.setUpdatedAt(Instant.now());
        return mapper.toOrganization(o, true);
    }

    public OrganizationResponse resubmit(Long id) {
        Organization o = requireOwned(id);
        if (o.getStatus() != OrganizationStatus.REJECTED) {
            throw ApiException.conflict("INVALID_STATUS", "Повторно подать можно только отклонённую заявку");
        }
        o.setStatus(OrganizationStatus.PENDING);
        o.setRejectReason(null);
        o.setDecidedBy(null);
        o.setDecidedAt(null);
        o.setUpdatedAt(Instant.now());
        return mapper.toOrganization(o, true);
    }

    // ---------- публичное

    @Transactional(readOnly = true)
    public PageResponse<OrganizationResponse> catalog(String q, String sport, String city, Integer page,
            Integer size) {
        Specification<Organization> spec = hasStatus(OrganizationStatus.APPROVED)
                .and(matches(q))
                .and(containsIgnoreCase("sportsDescription", sport))
                .and(equalsIgnoreCase("city", city));
        var pageable = PageResponse.pageable(page, size, Sort.by("name"));
        return PageResponse.of(organizations.findAll(spec, pageable), o -> mapper.toOrganization(o, false));
    }

    @Transactional(readOnly = true)
    public OrganizationResponse get(Long id) {
        Organization o = requireVisible(id);
        return mapper.toOrganization(o, isPrivileged(o));
    }

    @Transactional(readOnly = true)
    public List<EventShortResponse> events(Long id) {
        requireVisible(id);
        return events.findByOrganizerOrgIdAndStatusOrderByStartsAtAsc(id, EventStatus.PUBLISHED).stream()
                .map(mapper::toEventShort)
                .toList();
    }

    /** Для POST /events и комментариев от имени организации: владелец + approved, иначе 403. */
    @Transactional(readOnly = true)
    public Organization requireApprovedOwned(Long orgId, Long userId) {
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

    // ---------- админ

    @Transactional(readOnly = true)
    public PageResponse<OrganizationResponse> adminList(OrganizationStatus status, String q, Integer page,
            Integer size) {
        Specification<Organization> spec = hasStatus(status).and(matches(q));
        var pageable = PageResponse.pageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.of(organizations.findAll(spec, pageable), o -> mapper.toOrganization(o, true));
    }

    @Transactional(readOnly = true)
    public AdminOrganizationDetailsResponse adminDetails(Long id) {
        Organization o = require(id);
        var owner = users.findById(o.getOwnerId()).map(mapper::toUser).orElse(null);
        var orgEvents = events.findByOrganizerOrgIdOrderByStartsAtDesc(id).stream()
                .map(mapper::toEventShort)
                .toList();
        return new AdminOrganizationDetailsResponse(mapper.toOrganization(o, true), owner, orgEvents);
    }

    public OrganizationResponse approve(Long id) {
        User admin = currentUser.require();
        Organization o = requirePending(id);
        o.setStatus(OrganizationStatus.APPROVED);
        o.setRejectReason(null);
        o.setDecidedBy(admin.getId());
        o.setDecidedAt(Instant.now());
        o.setUpdatedAt(o.getDecidedAt());
        audit.organization(admin.getId(), id, AuditAction.ORG_APPROVED, null);
        notificationService.notify(o.getOwnerId(), NotificationType.ORG_APPROVED, "Организация допущена",
                "«" + o.getName() + "» одобрена: страница опубликована, можно создавать события.", null);
        return mapper.toOrganization(o, true);
    }

    public OrganizationResponse reject(Long id, String reason) {
        User admin = currentUser.require();
        Organization o = requirePending(id);
        o.setStatus(OrganizationStatus.REJECTED);
        o.setRejectReason(reason);
        o.setDecidedBy(admin.getId());
        o.setDecidedAt(Instant.now());
        o.setUpdatedAt(o.getDecidedAt());
        audit.organization(admin.getId(), id, AuditAction.ORG_REJECTED, reason);
        notificationService.notify(o.getOwnerId(), NotificationType.ORG_REJECTED, "Заявка организации отклонена",
                "«" + o.getName() + "»: " + reason, null);
        return mapper.toOrganization(o, true);
    }

    // ---------- helpers

    private Organization require(Long id) {
        return organizations.findById(id).orElseThrow(() -> ApiException.notFound("Организация не найдена"));
    }

    private Organization requirePending(Long id) {
        Organization o = require(id);
        if (o.getStatus() != OrganizationStatus.PENDING) {
            throw ApiException.conflict("ALREADY_REVIEWED", "Заявка уже рассмотрена",
                    Map.of("status", o.getStatus().name().toLowerCase()));
        }
        return o;
    }

    private Organization requireOwned(Long id) {
        Long me = currentUser.require().getId();
        Organization o = require(id);
        if (!me.equals(o.getOwnerId())) {
            throw ApiException.forbidden("Вы не владелец этой организации");
        }
        return o;
    }

    /** Не approved → 404 для всех, кроме владельца и админа. */
    private Organization requireVisible(Long id) {
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

    private static Specification<Organization> hasStatus(OrganizationStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<Organization> matches(String q) {
        if (q == null || q.isBlank()) {
            return (root, query, cb) -> null;
        }
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(cb.coalesce(root.get("description"), "")), like));
    }

    private static Specification<Organization> containsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return (root, query, cb) -> null;
        }
        String like = "%" + value.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), like);
    }

    private static Specification<Organization> equalsIgnoreCase(String field, String value) {
        if (value == null || value.isBlank()) {
            return (root, query, cb) -> null;
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get(field)), value.trim().toLowerCase());
    }
}
