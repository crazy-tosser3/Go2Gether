package com.example.STOLOTO.config;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.JoinMode;
import com.example.STOLOTO.Models.Level;
import com.example.STOLOTO.Models.Organization;
import com.example.STOLOTO.Models.OrganizationStatus;
import com.example.STOLOTO.Models.OrganizationType;
import com.example.STOLOTO.Models.OrganizerType;
import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.Sport;
import com.example.STOLOTO.Models.SportCategory;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.SportRepository;
import com.example.STOLOTO.repository.UserRepository;
import com.example.STOLOTO.Services.AuthService;
import com.example.STOLOTO.Services.SportService;

import lombok.RequiredArgsConstructor;

/** Идемпотентно заполняет Redis демо-данными: направления, демо-аккаунты, организации, пара событий. */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEMO_PASSWORD = "demo12345";

    private record PresetSport(String name, SportCategory category, String icon) {
    }

    private static final List<PresetSport> SPORTS = List.of(
            new PresetSport("Футбол", SportCategory.TEAM, "⚽"),
            new PresetSport("Баскетбол", SportCategory.TEAM, "🏀"),
            new PresetSport("Волейбол", SportCategory.TEAM, "🏐"),
            new PresetSport("Хоккей", SportCategory.TEAM, "🏒"),
            new PresetSport("Бег", SportCategory.INDIVIDUAL, "🏃"),
            new PresetSport("Теннис", SportCategory.INDIVIDUAL, "🎾"),
            new PresetSport("Плавание", SportCategory.INDIVIDUAL, "🏊"),
            new PresetSport("Велоспорт", SportCategory.INDIVIDUAL, "🚴"),
            new PresetSport("Бокс", SportCategory.INDIVIDUAL, "🥊"),
            new PresetSport("Шахматы", SportCategory.MIND, "♟"),
            new PresetSport("Йога", SportCategory.FITNESS, "🧘"),
            new PresetSport("Кроссфит", SportCategory.FITNESS, "🏋"));

    private final SportService sportService;
    private final SportRepository sports;
    private final UserRepository users;
    private final OrganizationRepository organizations;
    private final EventRepository events;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        SPORTS.forEach(p -> {
            Sport s = sportService.findOrCreate(p.name(), p.category(), null);
            if (s.getIcon() == null) {
                s.setIcon(p.icon());
                sports.save(s);
            }
        });

        String passwordHash = passwordEncoder.encode(DEMO_PASSWORD);
        demoUser("user", "Иван Петров", Role.USER, passwordHash);
        demoUser("user2", "Мария Смирнова", Role.USER, passwordHash);
        demoUser("org_pending", "Олег (клуб на модерации)", Role.USER, passwordHash);
        demoUser("org_approved", "Анна (допущенный клуб)", Role.USER, passwordHash);
        demoUser("admin", "Администратор", Role.ADMIN, passwordHash);

        demoOrganization("org-demo-pending", "org_pending", "Беговой клуб «Рассвет»", OrganizationStatus.PENDING);
        Organization approved = demoOrganization("org-demo-approved", "org_approved", "Футбольный клуб «Спартак-Двор»",
                OrganizationStatus.APPROVED);

        if (events.count() == 0) {
            String football = sportService.findOrCreate("Футбол", SportCategory.TEAM, null).getId();
            String running = sportService.findOrCreate("Бег", SportCategory.INDIVIDUAL, null).getId();
            demoEvent("Дворовый футбол 5×5", football, approved.getId(), JoinMode.OPEN, 10, Duration.ofDays(3));
            demoEvent("Отбор в команду на сезон", football, approved.getId(), JoinMode.APPROVAL, 20, Duration.ofDays(7));
            demoEvent("Утренняя пробежка в парке", running, null, JoinMode.OPEN, null, Duration.ofDays(2));
        }
        log.info("Демо-данные готовы. Пароль демо-аккаунтов: {}", DEMO_PASSWORD);
    }

    private void demoUser(String id, String name, Role role, String passwordHash) {
        if (users.existsById(id)) {
            return;
        }
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmail(AuthService.normalizeEmail(id.replace('_', '.') + "@demo.local"));
        u.setPasswordHash(passwordHash);
        u.setRole(role);
        u.setCity("Москва");
        u.setLevel(Level.AMATEUR);
        u.setDemo(true);
        u.setCreatedAt(Instant.now());
        users.save(u);
    }

    private Organization demoOrganization(String id, String ownerId, String name, OrganizationStatus status) {
        return organizations.findById(id).orElseGet(() -> {
            Organization o = new Organization();
            o.setId(id);
            o.setOwnerId(ownerId);
            o.setName(name);
            o.setDescription("Демо-организация");
            o.setType(OrganizationType.CLUB);
            o.setCity("Москва");
            o.setContacts("+7 900 000-00-00, " + ownerId.replace('_', '.') + "@demo.local");
            o.setStatus(status);
            o.setCreatedAt(Instant.now());
            if (status == OrganizationStatus.APPROVED) {
                o.setReviewedBy("admin");
                o.setReviewedAt(Instant.now());
            }
            return organizations.save(o);
        });
    }

    private void demoEvent(String title, String sportId, String orgId, JoinMode joinMode, Integer capacity,
            Duration in) {
        Event e = new Event();
        e.setId(UUID.randomUUID().toString());
        e.setTitle(title);
        e.setDescription("Демо-мероприятие");
        e.setSportId(sportId);
        e.setOrganizerType(orgId == null ? OrganizerType.USER : OrganizerType.ORGANIZATION);
        e.setOrganizerUserId(orgId == null ? "user2" : "org_approved");
        e.setOrganizerOrgId(orgId);
        e.setStartsAt(Instant.now().plus(in).truncatedTo(ChronoUnit.HOURS));
        e.setCity("Москва");
        e.setAddress("Москва, Парк Горького");
        e.setLocationNote("Встречаемся у главного входа");
        e.setLevel(Level.AMATEUR);
        e.setJoinMode(joinMode);
        e.setCapacity(capacity);
        e.setStatus(EventStatus.PUBLISHED);
        e.setCreatedAt(Instant.now());
        events.save(e);
    }
}
