package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            u.setAvatarUrl(blankToNull(r.avatarUrl()));
        }
        if (r.sportIds() != null) {
            u.setSportIds(new LinkedHashSet<>(sportService.validateIds(r.sportIds(), "sport_ids")));
        }
        if (r.level() != null) {
            u.setLevel(r.level());
        }
        u.setUpdatedAt(Instant.now());
        regions.userChanged(u.getId());
        return mapper.toUser(users.save(u));
    }

    /**
     * Удаление аккаунта (DELETE /users/me). Строка users не удаляется: на неё ссылаются события,
     * организации и журнал модерации (ON DELETE RESTRICT), а записи и обсуждения должны остаться
     * в истории. Вместо этого аккаунт деактивируется и обезличивается:
     * <ul>
     *   <li>предстоящие опубликованные события пользователя отменяются, участники получают уведомление;</li>
     *   <li>его активные записи отменяются, освободившиеся места уходят листу ожидания;</li>
     *   <li>личные данные стираются, email освобождается для новой регистрации;</li>
     *   <li>уведомления удаляются, текущий токен отзывается (остальные перестают работать из-за is_active).</li>
     * </ul>
     * Комментарии остаются и показываются от «Удалённого пользователя».
     */
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
                    "Сначала передайте или удалите свои организации — у организации должен быть владелец");
        }

        events.findByOrganizerUserIdAndStatusOrderByStartsAtDesc(u.getId(), EventStatus.PUBLISHED).stream()
                .filter(e -> !e.isPast())
                .forEach(e -> eventService.cancel(e.getId(), "Организатор удалил аккаунт"));
        participationService.cancelAllForUser(u.getId());
        notifications.deleteByRecipientId(u.getId());

        u.setActive(false);
        u.setName("Удалённый пользователь");
        u.setEmail("deleted+" + u.getId() + "@deleted.local");
        // Не хеш ни одного пароля — войти по паролю невозможно
        u.setPasswordHash("!deleted");
        u.setAbout(null);
        u.setCity(null);
        u.setAvatarUrl(null);
        u.setLevel(null);
        u.getSportIds().clear();
        u.setUpdatedAt(Instant.now());

        currentUser.tokenId().ifPresent(jwtService::revoke);
        regions.userChanged(u.getId());
    }

    /** События, где я организатор: созданные лично и от моих организаций. */
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
