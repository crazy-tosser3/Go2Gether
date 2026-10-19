package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.event.OrganizerEventResponse;
import com.example.STOLOTO.DTO.participation.MyParticipationResponse;
import com.example.STOLOTO.DTO.user.PublicUserResponse;
import com.example.STOLOTO.DTO.user.UpdateProfileRequest;
import com.example.STOLOTO.DTO.user.UserResponse;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.Organization;
import com.example.STOLOTO.Models.ParticipationStatus;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.cache.CacheRegions;
import com.example.STOLOTO.cache.PageCache;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.ParticipationRepository;
import com.example.STOLOTO.repository.UserRepository;
import com.example.STOLOTO.security.CurrentUser;

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
    private final SportService sportService;
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
