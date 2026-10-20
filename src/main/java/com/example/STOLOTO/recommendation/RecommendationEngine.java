package com.example.STOLOTO.recommendation;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.STOLOTO.DTO.recommendation.RecommendationReason;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.Level;
import com.example.STOLOTO.Models.SportCategory;
import com.example.STOLOTO.Models.TimeSlot;

@Component
public class RecommendationEngine {
    static final double W_INTEREST = 3.0;
    static final double W_PLAYED = 2.5;
    static final double W_CATEGORY = 1.0;
    static final double W_CITY = 2.0;
    static final double W_OTHER_CITY = -1.0;
    static final double W_NEARBY = 2.0;
    static final double W_LEVEL = 1.0;
    static final double W_FRIENDS = 1.2;
    static final double W_ORGANIZER = 1.0;
    static final double W_POPULAR = 0.6;
    static final double W_SOON = 1.0;
    static final double W_FULL = -1.5;
    static final double W_TIME = 1.0;

    static final double W_SIM_SPORT = 4.0;
    static final double W_SIM_CATEGORY = 1.5;
    static final double W_SIM_PARTICIPANTS = 4.0;
    static final double W_SIM_ORGANIZER = 1.5;

    static final double DIVERSITY_DECAY = 0.85;
    static final double NEARBY_KM = 10.0;
    static final double POPULAR_CONFIRMED = 10;
    static final double SOON_DAYS = 3;
    private static final double REASON_MIN = 0.3;

    public record Profile(Set<Long> interests, Map<Long, Double> sportAffinity,
            Map<SportCategory, Double> categoryAffinity, Map<String, Double> cityAffinity, boolean hasCity,
            Level level, Map<Long, Level> sportLevels, Map<Long, Integer> neighbours, Set<String> organizers,
            Double lat, Double lng, Double radiusKm, Set<TimeSlot> preferredTimes, ZoneId zone) {
        public static Profile empty(Double lat, Double lng) {
            return new Profile(Set.of(), Map.of(), Map.of(), Map.of(), false, null, Map.of(), Map.of(), Set.of(),
                    lat, lng, null, Set.of(), ZoneOffset.UTC);
        }

        Level levelFor(Long sportId) {
            Level own = sportLevels.get(sportId);
            return own != null ? own : level;
        }

        double nearbyKm() {
            return radiusKm != null ? radiusKm : NEARBY_KM;
        }
    }

    public record Candidate(Event event, SportCategory category, Set<Long> participants, long confirmed) {
    }

    public record Scored(Candidate candidate, double score, List<RecommendationReason> reasons) {
    }

    public List<Scored> recommend(Profile profile, List<Candidate> candidates, int limit, Instant now) {
        List<Scored> scored = new ArrayList<>(candidates.size());
        for (Candidate c : candidates) {
            scored.add(score(profile, c, now));
        }
        return diversify(scored, limit);
    }

    Scored score(Profile p, Candidate c, Instant now) {
        Event e = c.event();
        Contributions s = new Contributions();

        if (p.interests().contains(e.getSportId())) {
            s.add(RecommendationReason.INTEREST, W_INTEREST);
        }
        s.add(RecommendationReason.PLAYED_BEFORE, W_PLAYED * p.sportAffinity().getOrDefault(e.getSportId(), 0.0));
        s.add(null, W_CATEGORY * p.categoryAffinity().getOrDefault(c.category(), 0.0));

        String city = normalize(e.getCity());
        if (city != null && !p.cityAffinity().isEmpty()) {
            Double w = p.cityAffinity().get(city);
            if (w != null) {
                s.add(RecommendationReason.SAME_CITY, W_CITY * w);
            } else if (p.hasCity()) {
                s.add(null, W_OTHER_CITY);
            }
        }
        Double km = distanceKm(p.lat(), p.lng(), e.getLat(), e.getLng());
        if (km != null) {
            double radius = p.nearbyKm();
            s.add(km <= radius ? RecommendationReason.NEARBY : null, W_NEARBY * Math.exp(-km / radius));
        }

        s.add(RecommendationReason.LEVEL_MATCH, levelScore(p.levelFor(e.getSportId()), e.getLevel()) * W_LEVEL);

        if (e.getStartsAt() != null && !p.preferredTimes().isEmpty()) {
            ZonedDateTime start = e.getStartsAt().atZone(p.zone());
            if (p.preferredTimes().stream().anyMatch(t -> t.matches(start))) {
                s.add(RecommendationReason.PREFERRED_TIME, W_TIME);
            }
        }

        double together = 0;
        for (Long u : c.participants()) {
            Integer times = p.neighbours().get(u);
            if (times != null) {
                together += Math.log1p(times);
            }
        }
        s.add(RecommendationReason.FRIENDS_GOING, W_FRIENDS * Math.log1p(together));

        if (p.organizers().contains(organizerKey(e))) {
            s.add(RecommendationReason.FAMILIAR_ORGANIZER, W_ORGANIZER);
        }

        common(s, c, now);
        return s.toScored(c);
    }

    public List<Scored> similar(Candidate target, List<Candidate> candidates, int limit, Instant now) {
        Event t = target.event();
        List<Scored> scored = new ArrayList<>();
        for (Candidate c : candidates) {
            Event e = c.event();
            if (e.getId().equals(t.getId())) {
                continue;
            }
            Contributions s = new Contributions();
            if (e.getSportId() != null && e.getSportId().equals(t.getSportId())) {
                s.add(RecommendationReason.SAME_SPORT, W_SIM_SPORT);
            } else if (c.category() != null && c.category() == target.category()
                    && c.category() != SportCategory.OTHER) {
                s.add(RecommendationReason.SAME_CATEGORY, W_SIM_CATEGORY);
            }

            Double km = distanceKm(t.getLat(), t.getLng(), e.getLat(), e.getLng());
            if (km != null) {
                s.add(km <= NEARBY_KM ? RecommendationReason.NEARBY : null, W_NEARBY * Math.exp(-km / NEARBY_KM));
            } else {
                String a = normalize(t.getCity());
                if (a != null && a.equals(normalize(e.getCity()))) {
                    s.add(RecommendationReason.SAME_CITY, W_CITY);
                }
            }

            s.add(RecommendationReason.LEVEL_MATCH, levelScore(t.getLevel(), e.getLevel()) * W_LEVEL);
            s.add(RecommendationReason.SHARED_PARTICIPANTS,
                    W_SIM_PARTICIPANTS * jaccard(target.participants(), c.participants()));
            if (organizerKey(t).equals(organizerKey(e))) {
                s.add(RecommendationReason.SAME_ORGANIZER, W_SIM_ORGANIZER);
            }
            common(s, c, now);
            scored.add(s.toScored(c));
        }
        return diversify(scored, limit);
    }

    private static void common(Contributions s, Candidate c, Instant now) {
        Event e = c.event();
        double popularity = Math.min(1.0, Math.log1p(c.confirmed()) / Math.log1p(20));
        s.add(c.confirmed() >= POPULAR_CONFIRMED ? RecommendationReason.POPULAR : null, W_POPULAR * popularity);

        if (e.getStartsAt() != null) {
            double days = Math.max(0, Duration.between(now, e.getStartsAt()).toHours() / 24.0);
            s.add(days <= SOON_DAYS ? RecommendationReason.SOON : null, W_SOON / (1 + days / 7));
        }
        if (e.getCapacity() != null && c.confirmed() >= e.getCapacity()) {
            s.add(null, W_FULL);
        }
    }

    static List<Scored> diversify(List<Scored> scored, int limit) {
        List<Scored> pool = new ArrayList<>(scored);
        pool.sort(Comparator.comparingDouble(Scored::score).reversed());
        List<Scored> result = new ArrayList<>(Math.min(limit, pool.size()));
        Map<Long, Integer> picked = new HashMap<>();
        while (result.size() < limit && !pool.isEmpty()) {
            int best = 0;
            double bestValue = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < pool.size(); i++) {
                Scored s = pool.get(i);
                if (s.score() <= bestValue) {
                    break;
                }
                int same = picked.getOrDefault(s.candidate().event().getSportId(), 0);
                double value = s.score() >= 0 ? s.score() * Math.pow(DIVERSITY_DECAY, same) : s.score();
                if (value > bestValue) {
                    bestValue = value;
                    best = i;
                }
            }
            Scored chosen = pool.remove(best);
            picked.merge(chosen.candidate().event().getSportId(), 1, Integer::sum);
            result.add(chosen);
        }
        return result;
    }

    static double levelScore(Level a, Level b) {
        if (a == null || b == null) {
            return 0;
        }
        int diff = Math.abs(a.ordinal() - b.ordinal());
        return diff == 0 ? 1 : diff == 1 ? 0 : -1;
    }

    static double jaccard(Set<Long> a, Set<Long> b) {
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        long common = a.stream().filter(b::contains).count();
        return (double) common / (a.size() + b.size() - common);
    }

    static Double distanceKm(Double lat1, Double lng1, Double lat2, Double lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return null;
        }
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dLng / 2), 2);
        return 2 * 6371.0 * Math.asin(Math.sqrt(h));
    }

    public static String organizerKey(Event e) {
        return e.getOrganizerOrgId() != null ? "o:" + e.getOrganizerOrgId() : "u:" + e.getOrganizerUserId();
    }

    public static String normalize(String city) {
        return city == null || city.isBlank() ? null : city.trim().toLowerCase(Locale.ROOT);
    }

    private static final class Contributions {
        private double total;
        private final Map<RecommendationReason, Double> byReason = new EnumMap<>(RecommendationReason.class);

        void add(RecommendationReason reason, double value) {
            total += value;
            if (reason != null && value > 0) {
                byReason.merge(reason, value, Double::sum);
            }
        }

        Scored toScored(Candidate c) {
            List<RecommendationReason> reasons = byReason.entrySet().stream()
                    .filter(en -> en.getValue() >= REASON_MIN)
                    .sorted(Map.Entry.<RecommendationReason, Double>comparingByValue().reversed())
                    .map(Map.Entry::getKey)
                    .toList();
            return new Scored(c, Math.round(total * 100) / 100.0, reasons);
        }
    }
}
