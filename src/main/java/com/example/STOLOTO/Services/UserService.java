package com.example.STOLOTO.Services;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.STOLOTO.DTO.event.OrganizerEventResponse;
import com.example.STOLOTO.DTO.participation.MyParticipationResponse;
import com.example.STOLOTO.DTO.user.PublicUserResponse;
import com.example.STOLOTO.DTO.user.UpdateProfileRequest;
import com.example.STOLOTO.DTO.user.UserResponse;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.Participation;
import com.example.STOLOTO.Models.ParticipationStatus;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.ParticipationRepository;
import com.example.STOLOTO.repository.UserRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository users;
    private final EventRepository events;
    private final OrganizationRepository organizations;
    private final ParticipationRepository participations;
    private final SportService sportService;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    public PublicUserResponse publicProfile(String id) {
        User u = users.findById(id).orElseThrow(() -> ApiException.notFound("Пользователь не найден"));
        var created = events.findByOrganizerUserId(id).stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .sorted(Comparator.comparing(Event::getStartsAt).reversed())
                .map(mapper::toEventShort)
                .toList();
        return new PublicUserResponse(u.getId(), u.getName(), u.getAvatarUrl(), u.getCity(), u.getBio(),
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
            u.setBio(blankToNull(r.bio()));
        }
        if (r.avatarUrl() != null) {
            u.setAvatarUrl(blankToNull(r.avatarUrl()));
        }
        if (r.sportIds() != null) {
            u.setSportIds(sportService.validateIds(r.sportIds(), "sport_ids"));
        }
        if (r.level() != null) {
            u.setLevel(r.level());
        }
        return mapper.toUser(users.save(u));
    }

    public List<OrganizerEventResponse> myEvents() {
        String me = currentUser.require().getId();
        var orgIds = organizations.findByOwnerId(me).stream().map(o -> o.getId()).toList();
        return events.findAll().stream()
                .filter(e -> me.equals(e.getOrganizerUserId())
                        || (e.getOrganizerOrgId() != null && orgIds.contains(e.getOrganizerOrgId())))
                .sorted(Comparator.comparing(Event::getStartsAt).reversed())
                .map(e -> new OrganizerEventResponse(mapper.toEventShort(e),
                        mapper.count(e.getId(), ParticipationStatus.PENDING),
                        mapper.count(e.getId(), ParticipationStatus.WAITLISTED)))
                .toList();
    }

    public List<MyParticipationResponse> myParticipations(ParticipationStatus status) {
        String me = currentUser.require().getId();
        return participations.findByUserId(me).stream()
                .filter(p -> status == null || p.getStatus() == status)
                .sorted(Comparator.comparing(Participation::getCreatedAt).reversed())
                .map(p -> new MyParticipationResponse(mapper.toParticipation(p),
                        events.findById(p.getEventId()).map(mapper::toEventShort).orElse(null)))
                .toList();
    }

    static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
