package com.example.STOLOTO.cache;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.example.STOLOTO.Models.Event;

import lombok.RequiredArgsConstructor;

/**
 * Регионы кэша страниц, их TTL и правила инвалидации: что сбрасывать при изменении сущности.
 */
@Component
@RequiredArgsConstructor
public class CacheRegions {

    public static final String FEED = "events-feed";
    public static final String ORGS = "orgs";
    public static final String SPORT_STATS = "sport-stats";

    public static final Duration FEED_TTL = Duration.ofSeconds(60);
    public static final Duration EVENT_TTL = Duration.ofMinutes(5);
    public static final Duration PARTICIPANTS_TTL = Duration.ofMinutes(2);
    public static final Duration COMMENTS_TTL = Duration.ofMinutes(2);
    public static final Duration ORG_TTL = Duration.ofMinutes(5);
    public static final Duration USER_TTL = Duration.ofMinutes(5);
    public static final Duration SPORT_STATS_TTL = Duration.ofMinutes(2);

    private final PageCache cache;

    public static String event(Long id) {
        return "event:" + id;
    }

    public static String comments(Long eventId) {
        return "comments:" + eventId;
    }

    public static String org(Long id) {
        return "org:" + id;
    }

    public static String user(Long id) {
        return "user:" + id;
    }

    /** Событие или его записи изменились: карточка, лента, счётчики у организатора и направления. */
    public void eventChanged(Event e) {
        cache.invalidate(FEED, SPORT_STATS, event(e.getId()),
                e.getOrganizerOrgId() != null ? org(e.getOrganizerOrgId()) : user(e.getOrganizerUserId()));
    }

    public void commentsChanged(Long eventId) {
        cache.invalidate(comments(eventId));
    }

    /** Название/статус организации видны в каталоге, на её странице и в ленте. */
    public void organizationChanged(Long orgId) {
        cache.invalidate(ORGS, org(orgId), FEED);
    }

    /** Имя/аватар пользователя видны в профиле и в ленте. */
    public void userChanged(Long userId) {
        cache.invalidate(user(userId), FEED);
    }
}
