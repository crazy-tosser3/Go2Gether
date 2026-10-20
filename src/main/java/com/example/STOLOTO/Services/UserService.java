package com.example.STOLOTO.Services;

import java.time.Instant;
import java.time.Year;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.STOLOTO.DTO.event.OrganizerEventResponse;
import com.example.STOLOTO.DTO.participation.MyParticipationResponse;
import com.example.STOLOTO.DTO.user.DeleteAccountRequest;
import com.example.STOLOTO.DTO.user.PublicUserResponse;
import com.example.STOLOTO.DTO.user.UpdateProfileRequest;
import com.example.STOLOTO.DTO.user.UserResponse;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.Organization;
import com.example.STOLOTO.Models.ParticipationStatus;
import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.cache.CacheRegions;
import com.example.STOLOTO.cache.PageCache;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.NotificationRepository;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.ParticipationRepository;
import com.example.STOLOTO.repository.UserRepository;
import com.example.STOLOTO.security.CurrentUser;
import com.example.STOLOTO.security.JwtService;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    static final int MIN_BIRTH_YEAR = 1920;
    static final int MIN_AGE = 10;

    private final UserRepository users;
    private final EventRepository events;
    private final OrganizationRepository organizations;
    private final ParticipationRepository participations;
    private final NotificationRepository notifications;
    private final SportService sportService;
    private final EventService eventService;
    private final ParticipationService participationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;
    private final PageCache cache;
    private final CacheRegions regions;
    private final AvatarStorage avatars;

    @Transactional(readOnly = true)
    public PublicUserResponse publicProfile(Long id) {
        return cache.get(CacheRegions.user(id), "public", CacheRegions.USER_TTL,
                new TypeReference<PublicUserResponse>() {}, () -> loadPublicProfile(id));
    }

    private PublicUserResponse loadPublicProfile(Long id) {
        User u = users.findById(id).filter(User::isActive)
                .orElseThrow(() -> ApiException.notFound("Пользователь не найден"));
        var created = events.findByOrganizerUserIdAndStatusOrderByStartsAtDesc(id, EventStatus.PUBLISHED).stream()
                .map(mapper::toEventShort)
                .toList();
        return new PublicUserResponse(u.getId(), u.getName(), u.getAvatarUrl(), u.getCity(), u.getAbout(),
                u.getLevel(), mapper.sports(u.getSportIds()), created);
    }

    public UserResponse updateMe(UpdateProfileRequest r) {
        User u = currentUser.require();
        if (r.name() != null) {
            if (r.name().isBlank()) {
                throw ApiException.validation("name", "не должно быть пустым");
            }
            u.setName(r.name().trim());
        }
        if (r.city() != null) {
            u.setCity(blankToNull(r.city()));
        }
        if (r.bio() != null) {
            u.setAbout(blankToNull(r.bio()));
        }
        if (r.avatarUrl() != null) {
            String avatarUrl = blankToNull(r.avatarUrl());
            if (avatarUrl != null && !avatarUrl.equals(u.getAvatarUrl()) && avatars.isUploaded(avatarUrl)) {
                throw ApiException.validation("avatar_url", "фото загружается через POST /users/me/avatar");
            }
            replaceAvatar(u, avatarUrl);
        }
        if (r.sportIds() != null) {
            u.setSportIds(new LinkedHashSet<>(sportService.validateIds(r.sportIds(), "sport_ids")));
            u.getSportLevels().keySet().retainAll(u.getSportIds());
        }
        if (r.level() != null) {
            u.setLevel(r.level());
        }
        if (r.sportLevels() != null) {
            u.getSportLevels().clear();
            r.sportLevels().forEach((sportId, level) -> {
                if (sportId == null || level == null || !u.getSportIds().contains(sportId)) {
                    throw ApiException.validation("sport_levels",
                            "уровень можно указать только для видов из sport_ids: " + sportId);
                }
                u.getSportLevels().put(sportId, level);
            });
        }
        if (r.birthYear() != null) {
            int maxYear = Year.now().getValue() - MIN_AGE;
            if (r.birthYear() < MIN_BIRTH_YEAR || r.birthYear() > maxYear) {
                throw ApiException.validation("birth_year",
                        "должен быть от " + MIN_BIRTH_YEAR + " до " + maxYear);
            }
            u.setBirthYear(r.birthYear());
        }
        if (r.radiusKm() != null) {
            u.setRadiusKm(r.radiusKm());
        }
        if (r.preferredTimes() != null) {
            if (r.preferredTimes().contains(null)) {
                throw ApiException.validation("preferred_times", "неизвестное значение");
            }
            u.getPreferredTimes().clear();
            u.getPreferredTimes().addAll(r.preferredTimes());
        }
        if (r.preferredAgeFrom() != null) {
            u.setPreferredAgeFrom(r.preferredAgeFrom());
        }
        if (r.preferredAgeTo() != null) {
            u.setPreferredAgeTo(r.preferredAgeTo());
        }
        if (u.getPreferredAgeFrom() != null && u.getPreferredAgeTo() != null
                && u.getPreferredAgeFrom() > u.getPreferredAgeTo()) {
            throw ApiException.validation("preferred_age_from", "не может быть больше preferred_age_to");
        }
        return saveProfile(u);
    }

    public void deleteMe(DeleteAccountRequest request) {
        User u = currentUser.require();
        if (!passwordEncoder.matches(request.password(), u.getPasswordHash())) {
            throw ApiException.forbidden("Неверный пароль");
        }
        if (u.isDemo()) {
            throw ApiException.forbidden("Демо-аккаунт удалить нельзя");
        }
        if (u.getRole() == Role.ADMIN && users.countByRoleAndActiveTrue(Role.ADMIN) <= 1) {
            throw ApiException.conflict("LAST_ADMIN", "Нельзя удалить последнего администратора");
        }
        if (!organizations.findByOwnerIdOrderByCreatedAtDesc(u.getId()).isEmpty()) {
            throw ApiException.conflict("HAS_ORGANIZATIONS",
                    "Сначала передайте или удалите свою организацию");
        }

        events.findByOrganizerUserIdAndStatusOrderByStartsAtDesc(u.getId(), EventStatus.PUBLISHED).stream()
                .filter(e -> !e.isPast())
                .forEach(e -> eventService.cancel(e.getId(), "Организатор удалил аккаунт"));
        participationService.cancelAllForUser(u.getId());
        notifications.deleteByRecipientId(u.getId());

        u.setActive(false);
        u.setName("Удалённый пользователь");
        u.setEmail("deleted+" + u.getId() + "@deleted.local");
        u.setPasswordHash("!deleted");
        u.setAbout(null);
        u.setCity(null);
        replaceAvatar(u, null);
        u.setLevel(null);
        u.getSportIds().clear();
        u.getSportLevels().clear();
        u.getPreferredTimes().clear();
        u.setBirthYear(null);
        u.setRadiusKm(null);
        u.setPreferredAgeFrom(null);
        u.setPreferredAgeTo(null);
        u.setUpdatedAt(Instant.now());

        currentUser.tokenId().ifPresent(jwtService::revoke);
        regions.userChanged(u.getId());
    }

    public UserResponse uploadAvatar(MultipartFile file) {
        User u = currentUser.require();
        replaceAvatar(u, avatars.store(file));
        return saveProfile(u);
    }

    public UserResponse deleteAvatar() {
        User u = currentUser.require();
        replaceAvatar(u, null);
        return saveProfile(u);
    }

    private void replaceAvatar(User u, String avatarUrl) {
        if (u.getAvatarUrl() != null && !u.getAvatarUrl().equals(avatarUrl)) {
            avatars.deleteAfterCommit(u.getAvatarUrl());
        }
        u.setAvatarUrl(avatarUrl);
    }

    private UserResponse saveProfile(User u) {
        u.setUpdatedAt(Instant.now());
        regions.userChanged(u.getId());
        return mapper.toUser(users.save(u));
    }

    @Transactional(readOnly = true)
    public List<OrganizerEventResponse> myEvents() {
        Long me = currentUser.require().getId();
        var orgIds = organizations.findByOwnerIdOrderByCreatedAtDesc(me).stream().map(Organization::getId).toList();
        return events.findByOrganizerUserIdOrOrganizerOrgIdInOrderByStartsAtDesc(me, orgIds).stream()
                .map(e -> new OrganizerEventResponse(mapper.toEventShort(e),
                        mapper.count(e.getId(), ParticipationStatus.PENDING),
                        mapper.count(e.getId(), ParticipationStatus.WAITLIST)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyParticipationResponse> myParticipations(ParticipationStatus status) {
        Long me = currentUser.require().getId();
        return participations.findByUserIdOrderByCreatedAtDesc(me).stream()
                .filter(p -> status == null || p.getStatus() == status)
                .map(p -> new MyParticipationResponse(mapper.toParticipation(p),
                        events.findById(p.getEventId()).map(mapper::toEventShort).orElse(null)))
                .toList();
    }

    static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
