package com.example.STOLOTO.Services;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.recommendation.RecommendationResponse;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.Organization;
import com.example.STOLOTO.Models.Participation;
import com.example.STOLOTO.Models.ParticipationStatus;
import com.example.STOLOTO.Models.Sport;
import com.example.STOLOTO.Models.SportCategory;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.cache.CacheRegions;
import com.example.STOLOTO.cache.PageCache;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.recommendation.RecommendationEngine;
import com.example.STOLOTO.recommendation.RecommendationEngine.Candidate;
import com.example.STOLOTO.recommendation.RecommendationEngine.Profile;
import com.example.STOLOTO.recommendation.RecommendationEngine.Scored;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.ParticipationRepository;
import com.example.STOLOTO.repository.SportRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecommendationService {

    public static final int DEFAULT_LIMIT = 10;
    public static final int MAX_LIMIT = 50;
    static final Duration HORIZON = Duration.ofDays(60);
    static final int MAX_CANDIDATES = 500;
    static final int MAX_HISTORY = 100;
    static final double UNCONFIRMED_WEIGHT = 0.6;

    private static final Set<ParticipationStatus> HISTORY = EnumSet.of(ParticipationStatus.CONFIRMED,
            ParticipationStatus.PENDING, ParticipationStatus.WAITLIST);

    private final EventRepository events;
    private final ParticipationRepository participations;
    private final SportRepository sports;
    private final OrganizationRepository organizations;
    private final EventService eventService;
    private final RecommendationEngine engine;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;
    private final PageCache cache;

    @Value("${app.zone:Asia/Krasnoyarsk}")
    private ZoneId zone;

    public List<RecommendationResponse> forCurrentUser(Integer limit, Double lat, Double lng) {
        int n = limit(limit);
        Instant now = Instant.now();
        User me = currentUser.find().orElse(null);
        List<Event> upcoming = upcoming(now);

        if (me == null) {
            return toResponses(engine.recommend(Profile.empty(lat, lng), candidates(upcoming), n, now));
        }

        List<Participation> mine = participations.findByUserIdOrderByCreatedAtDesc(me.getId());
        Set<Long> seen = mine.stream().map(Participation::getEventId).collect(Collectors.toSet());
        Set<Long> ownedOrgs = organizations.findByOwnerIdOrderByCreatedAtDesc(me.getId()).stream()
                .map(Organization::getId).collect(Collectors.toSet());
        List<Event> fresh = upcoming.stream()
                .filter(e -> !seen.contains(e.getId()))
                .filter(e -> !me.getId().equals(e.getOrganizerUserId()) && !ownedOrgs.contains(e.getOrganizerOrgId()))
                .toList();

        Profile profile = profile(me, mine, lat, lng);
        return toResponses(engine.recommend(profile, candidates(fresh), n, now));
    }

    public List<RecommendationResponse> similar(Long eventId, Integer limit) {
        int n = limit(limit);
        eventService.requireViewableCard(eventId);
        return cache.get(CacheRegions.FEED, "similar:" + eventId + "|n=" + n, CacheRegions.FEED_TTL,
                new TypeReference<List<RecommendationResponse>>() {}, () -> {
                    Instant now = Instant.now();
                    Event target = events.findById(eventId)
                            .orElseThrow(() -> ApiException.notFound("Событие не найдено"));
                    List<Event> pool = upcoming(now).stream().filter(e -> !e.getId().equals(eventId)).toList();
                    Candidate t = candidates(List.of(target)).getFirst();
                    return toResponses(engine.similar(t, candidates(pool), n, now));
                });
    }

    Profile profile(User me, List<Participation> mine, Double lat, Double lng) {
        List<Participation> history = mine.stream()
                .filter(p -> HISTORY.contains(p.getStatus()))
                .limit(MAX_HISTORY)
                .toList();
        Map<Long, Event> historyEvents = events.findAllById(history.stream().map(Participation::getEventId).toList())
                .stream().collect(Collectors.toMap(Event::getId, Function.identity()));
        Map<Long, SportCategory> categories = categories();

        Map<Long, Double> sportWeight = new HashMap<>();
        Map<SportCategory, Double> categoryWeight = new EnumMap<>(SportCategory.class);
        Map<String, Double> cityWeight = new HashMap<>();
        Set<String> organizers = new HashSet<>();
        double total = 0;
        for (Participation p : history) {
            Event e = historyEvents.get(p.getEventId());
            if (e == null) {
                continue;
            }
            boolean confirmed = p.getStatus() == ParticipationStatus.CONFIRMED;
            double w = confirmed ? 1.0 : UNCONFIRMED_WEIGHT;
            total += w;
            sportWeight.merge(e.getSportId(), w, Double::sum);
            SportCategory category = categories.get(e.getSportId());
            if (category != null) {
                categoryWeight.merge(category, w, Double::sum);
            }
            String city = RecommendationEngine.normalize(e.getCity());
            if (city != null) {
                cityWeight.merge(city, w, Double::sum);
            }
            if (confirmed) {
                organizers.add(RecommendationEngine.organizerKey(e));
            }
        }
        normalize(sportWeight, total);
        normalize(categoryWeight, total);
        normalize(cityWeight, total);
        String home = RecommendationEngine.normalize(me.getCity());
        if (home != null) {
            cityWeight.put(home, 1.0);
        }

        Map<Long, Integer> neighbours = new HashMap<>();
        if (!historyEvents.isEmpty()) {
            for (Participation p : participations.findByEventIdInAndStatusIn(historyEvents.keySet(),
                    EnumSet.of(ParticipationStatus.CONFIRMED))) {
                if (!p.getUserId().equals(me.getId())) {
                    neighbours.merge(p.getUserId(), 1, Integer::sum);
                }
            }
        }

        Double radius = me.getRadiusKm() == null ? null : me.getRadiusKm().doubleValue();
        return new Profile(Set.copyOf(me.getSportIds()), sportWeight, categoryWeight, cityWeight, home != null,
                me.getLevel(), Map.copyOf(me.getSportLevels()), neighbours, organizers, lat, lng, radius,
                Set.copyOf(me.getPreferredTimes()), zone);
    }

    private static <K> void normalize(Map<K, Double> weights, double total) {
        if (total > 0) {
            weights.replaceAll((k, v) -> v / total);
        }
    }

    private List<Event> upcoming(Instant now) {
        return events.findByStatusAndStartsAtBetweenOrderByStartsAtAsc(EventStatus.PUBLISHED, now, now.plus(HORIZON),
                PageRequest.of(0, MAX_CANDIDATES));
    }

    private List<Candidate> candidates(List<Event> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Map<Long, SportCategory> categories = categories();
        Map<Long, Set<Long>> participants = new HashMap<>();
        Map<Long, Long> confirmed = new HashMap<>();
        Collection<Long> ids = list.stream().map(Event::getId).toList();
        for (Participation p : participations.findByEventIdInAndStatusIn(ids, ParticipationStatus.ACTIVE)) {
            participants.computeIfAbsent(p.getEventId(), k -> new HashSet<>()).add(p.getUserId());
            if (p.getStatus() == ParticipationStatus.CONFIRMED) {
                confirmed.merge(p.getEventId(), 1L, Long::sum);
            }
        }
        return list.stream()
                .map(e -> new Candidate(e, categories.get(e.getSportId()),
                        participants.getOrDefault(e.getId(), Set.of()), confirmed.getOrDefault(e.getId(), 0L)))
                .toList();
    }

    private Map<Long, SportCategory> categories() {
        Map<Long, SportCategory> map = new LinkedHashMap<>();
        for (Sport s : sports.findAll()) {
            map.put(s.getId(), s.getCategory());
        }
        return map;
    }

    private List<RecommendationResponse> toResponses(List<Scored> scored) {
        return scored.stream()
                .map(s -> new RecommendationResponse(mapper.toEventShort(s.candidate().event()), s.score(),
                        s.reasons()))
                .toList();
    }

    private static int limit(Integer limit) {
        return limit == null || limit < 1 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
    }
}
