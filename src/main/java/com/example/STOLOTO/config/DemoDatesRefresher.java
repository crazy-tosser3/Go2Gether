package com.example.STOLOTO.config;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.Organization;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.cache.CacheRegions;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Демо-данные не «протухают»: при старте приложения прошедшие мероприятия демо-аккаунтов
 * сдвигаются вперёд на целое число недель (день недели и время сохраняются).
 * Мероприятия обычных пользователей не трогаются.
 * Выключить: app.demo.refresh-dates=false
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.refresh-dates", havingValue = "true", matchIfMissing = true)
public class DemoDatesRefresher implements ApplicationRunner {

    private static final Duration WEEK = Duration.ofDays(7);

    /** Опубликованные — для ленты; снятые — чтобы после «восстановить» на демо событие не оказалось в прошлом. */
    private static final Set<EventStatus> SHIFTABLE = EnumSet.of(EventStatus.PUBLISHED, EventStatus.UNPUBLISHED);

    private final UserRepository users;
    private final OrganizationRepository organizations;
    private final EventRepository events;
    private final CacheRegions regions;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Instant now = Instant.now();
        int moved = 0;
        for (User demo : users.findByDemoTrueOrderByIdAsc()) {
            List<Long> orgIds = organizations.findByOwnerIdOrderByCreatedAtDesc(demo.getId()).stream()
                    .map(Organization::getId)
                    .toList();
            for (Event e : events.findByOrganizerUserIdOrOrganizerOrgIdInOrderByStartsAtDesc(demo.getId(), orgIds)) {
                if (!SHIFTABLE.contains(e.getStatus()) || e.getStartsAt().isAfter(now)) {
                    continue;
                }
                long weeks = Duration.between(e.getStartsAt(), now).toDays() / 7 + 1;
                Duration shift = WEEK.multipliedBy(weeks);
                e.setStartsAt(e.getStartsAt().plus(shift));
                if (e.getEndsAt() != null) {
                    e.setEndsAt(e.getEndsAt().plus(shift));
                }
                regions.eventChanged(e);
                moved++;
            }
        }
        log.info("DemoDatesRefresher: сдвинуто в будущее демо-мероприятий: {}", moved);
    }
}