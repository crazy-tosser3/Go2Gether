package com.example.STOLOTO.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.STOLOTO.DTO.recommendation.RecommendationReason;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.Level;
import com.example.STOLOTO.Models.SportCategory;
import com.example.STOLOTO.Models.TimeSlot;
import com.example.STOLOTO.recommendation.RecommendationEngine.Candidate;
import com.example.STOLOTO.recommendation.RecommendationEngine.Profile;
import com.example.STOLOTO.recommendation.RecommendationEngine.Scored;

@DisplayName("REC: Движок рекомендаций")
class RecommendationEngineTest {

    private static final Instant NOW = Instant.parse("2026-10-20T10:00:00Z");
    private static final long FOOTBALL = 1, TENNIS = 2, CHESS = 3;

    private final RecommendationEngine engine = new RecommendationEngine();

    @Test
    @DisplayName("Интерес из профиля и свой город поднимают событие наверх")
    void interestAndCityWin() {
        Profile p = profile(Set.of(TENNIS), Map.of(), Map.of("томск", 1.0), Level.AMATEUR, Map.of(), Set.of());
        var tennis = candidate(event(1, TENNIS, "Томск", Level.AMATEUR, 5), SportCategory.INDIVIDUAL, Set.of(), 0);
        var football = candidate(event(2, FOOTBALL, "Москва", Level.PRO, 5), SportCategory.TEAM, Set.of(), 0);

        List<Scored> result = engine.recommend(p, List.of(football, tennis), 10, NOW);

        assertThat(result).extracting(s -> s.candidate().event().getId()).containsExactly(1L, 2L);
        assertThat(result.getFirst().reasons()).contains(RecommendationReason.INTEREST, RecommendationReason.SAME_CITY,
                RecommendationReason.LEVEL_MATCH);
    }

    @Test
    @DisplayName("Событие, куда идут знакомые, выше такого же без них")
    void friendsGoing() {
        Profile p = profile(Set.of(), Map.of(), Map.of(), null, Map.of(42L, 3), Set.of());
        var withFriend = candidate(event(1, CHESS, null, null, 5), SportCategory.MIND, Set.of(42L, 7L), 2);
        var alone = candidate(event(2, CHESS, null, null, 5), SportCategory.MIND, Set.of(8L, 9L), 2);

        List<Scored> result = engine.recommend(p, List.of(alone, withFriend), 10, NOW);

        assertThat(result.getFirst().candidate().event().getId()).isEqualTo(1L);
        assertThat(result.getFirst().reasons()).contains(RecommendationReason.FRIENDS_GOING);
    }

    @Test
    @DisplayName("Гость без профиля получает популярные события выше пустых")
    void coldStartUsesPopularity() {
        var popular = candidate(event(1, FOOTBALL, null, null, 10), SportCategory.TEAM, Set.of(), 15);
        var empty = candidate(event(2, TENNIS, null, null, 10), SportCategory.INDIVIDUAL, Set.of(), 0);

        List<Scored> result = engine.recommend(Profile.empty(null, null), List.of(empty, popular), 10, NOW);

        assertThat(result.getFirst().candidate().event().getId()).isEqualTo(1L);
        assertThat(result.getFirst().reasons()).contains(RecommendationReason.POPULAR);
    }

    @Test
    @DisplayName("Разнообразие: при равных баллах в топе не только один вид спорта")
    void diversity() {
        Profile p = profile(Set.of(FOOTBALL, TENNIS), Map.of(), Map.of(), null, Map.of(), Set.of());
        var f1 = candidate(event(1, FOOTBALL, null, null, 5), SportCategory.TEAM, Set.of(), 0);
        var f2 = candidate(event(2, FOOTBALL, null, null, 5), SportCategory.TEAM, Set.of(), 0);
        var f3 = candidate(event(3, FOOTBALL, null, null, 5), SportCategory.TEAM, Set.of(), 0);
        var t1 = candidate(event(4, TENNIS, null, null, 5), SportCategory.INDIVIDUAL, Set.of(), 0);

        List<Scored> top2 = engine.recommend(p, List.of(f1, f2, f3, t1), 2, NOW);

        assertThat(top2).extracting(s -> s.candidate().event().getSportId()).containsExactlyInAnyOrder(FOOTBALL, TENNIS);
    }

    @Test
    @DisplayName("Похожие: тот же спорт и общие участники важнее, сам себя не предлагает")
    void similar() {
        var target = candidate(event(1, FOOTBALL, "Томск", Level.AMATEUR, 5), SportCategory.TEAM, Set.of(1L, 2L, 3L), 3);
        var sameSport = candidate(event(2, FOOTBALL, "Томск", Level.AMATEUR, 6), SportCategory.TEAM, Set.of(1L, 2L), 2);
        var other = candidate(event(3, CHESS, "Москва", Level.PRO, 6), SportCategory.MIND, Set.of(9L), 1);

        List<Scored> result = engine.similar(target, List.of(target, other, sameSport), 10, NOW);

        assertThat(result).extracting(s -> s.candidate().event().getId()).containsExactly(2L, 3L);
        assertThat(result.getFirst().reasons()).contains(RecommendationReason.SAME_SPORT,
                RecommendationReason.SHARED_PARTICIPANTS, RecommendationReason.SAME_CITY);
    }

    @Test
    @DisplayName("Уровень по виду спорта важнее общего уровня")
    void sportLevelOverridesGeneral() {
        Profile p = new Profile(Set.of(), Map.of(), Map.of(), Map.of(), false, Level.BEGINNER,
                Map.of(TENNIS, Level.PRO), Map.of(), Set.of(), null, null, null, Set.of(), ZoneOffset.UTC);
        var proTennis = candidate(event(1, TENNIS, null, Level.PRO, 5), SportCategory.INDIVIDUAL, Set.of(), 0);
        var beginnerTennis = candidate(event(2, TENNIS, null, Level.BEGINNER, 5), SportCategory.INDIVIDUAL, Set.of(), 0);

        List<Scored> result = engine.recommend(p, List.of(beginnerTennis, proTennis), 10, NOW);

        assertThat(result.getFirst().candidate().event().getId()).isEqualTo(1L);
        assertThat(result.getFirst().reasons()).contains(RecommendationReason.LEVEL_MATCH);
    }

    @Test
    @DisplayName("Удобное время даёт бонус и причину preferred_time")
    void preferredTime() {
        Profile p = new Profile(Set.of(), Map.of(), Map.of(), Map.of(), false, null, Map.of(), Map.of(), Set.of(),
                null, null, null, Set.of(TimeSlot.EVENING), ZoneOffset.UTC);
        var evening = candidate(event(1, CHESS, null, null, 5, 19), SportCategory.MIND, Set.of(), 0);
        var morning = candidate(event(2, CHESS, null, null, 5, 8), SportCategory.MIND, Set.of(), 0);

        List<Scored> result = engine.recommend(p, List.of(morning, evening), 10, NOW);

        assertThat(result.getFirst().candidate().event().getId()).isEqualTo(1L);
        assertThat(result.getFirst().reasons()).contains(RecommendationReason.PREFERRED_TIME);
        assertThat(result.getLast().reasons()).doesNotContain(RecommendationReason.PREFERRED_TIME);
    }

    @Test
    @DisplayName("Радиус из профиля задаёт границу «рядом»")
    void radiusFromProfile() {
        var e = event(1, CHESS, null, null, 5);
        e.setLat(56.10);
        e.setLng(92.85);
        var c = candidate(e, SportCategory.MIND, Set.of(), 0);
        Profile near = new Profile(Set.of(), Map.of(), Map.of(), Map.of(), false, null, Map.of(), Map.of(), Set.of(),
                56.01, 92.85, 20.0, Set.of(), ZoneOffset.UTC);
        Profile far = new Profile(Set.of(), Map.of(), Map.of(), Map.of(), false, null, Map.of(), Map.of(), Set.of(),
                56.01, 92.85, 5.0, Set.of(), ZoneOffset.UTC);

        assertThat(engine.recommend(near, List.of(c), 1, NOW).getFirst().reasons())
                .contains(RecommendationReason.NEARBY);
        assertThat(engine.recommend(far, List.of(c), 1, NOW).getFirst().reasons())
                .doesNotContain(RecommendationReason.NEARBY);
    }

    @Test
    @DisplayName("Расстояние: Томск — Новосибирск около 200 км")
    void distance() {
        assertThat(RecommendationEngine.distanceKm(56.4846, 84.9476, 55.0084, 82.9357)).isBetween(190.0, 215.0);
        assertThat(RecommendationEngine.distanceKm(null, 84.0, 55.0, 82.0)).isNull();
    }

    private static Profile profile(Set<Long> interests, Map<Long, Double> sports, Map<String, Double> cities,
            Level level, Map<Long, Integer> neighbours, Set<String> organizers) {
        return new Profile(interests, sports, Map.of(), cities, !cities.isEmpty(), level, Map.of(), neighbours,
                organizers, null, null, null, Set.of(), ZoneOffset.UTC);
    }

    private static Candidate candidate(Event e, SportCategory category, Set<Long> participants, long confirmed) {
        return new Candidate(e, category, participants, confirmed);
    }

    private static Event event(long id, long sportId, String city, Level level, int daysAhead, int hourUtc) {
        Event e = event(id, sportId, city, level, daysAhead);
        e.setStartsAt(NOW.plus(Duration.ofDays(daysAhead)).atZone(ZoneOffset.UTC).withHour(hourUtc).toInstant());
        return e;
    }

    private static Event event(long id, long sportId, String city, Level level, int daysAhead) {
        Event e = new Event();
        e.setId(id);
        e.setSportId(sportId);
        e.setCity(city);
        e.setLevel(level);
        e.setOrganizerUserId(100 + id);
        e.setStartsAt(NOW.plus(Duration.ofDays(daysAhead)));
        return e;
    }
}
