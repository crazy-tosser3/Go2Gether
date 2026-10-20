package com.example.STOLOTO.config;

import com.example.STOLOTO.Models.*;
import com.example.STOLOTO.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository         userRepo;
    private final SportRepository        sportRepo;
    private final OrganizationRepository orgRepo;
    private final EventRepository        eventRepo;
    private final ParticipationRepository partRepo;
    private final CommentRepository      commentRepo;
    private final NotificationRepository notifRepo;
    private final PasswordEncoder        passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepo.count() > 0) {
            log.info("DataSeeder: база уже заполнена, пропускаем.");
            return;
        }
        log.info("DataSeeder: наполняем базу демо-данными…");

        String hash = passwordEncoder.encode("Demo1234!");

        User userAlexey = user("user@demo.local",  hash, "Алексей Петров",
                "Люблю футбол и утренние пробежки", Role.USER,  Level.AMATEUR);
        User userMaria  = user("user2@demo.local", hash, "Мария Иванова",
                "Шахматы и йога — мой выбор",       Role.USER,  Level.BEGINNER);
        User userOrg    = user("org@demo.local",   hash, "Дмитрий Соколов",
                "Представитель спортивных клубов",  Role.USER,  Level.PRO);
        User userOrg2   = user("org2@demo.local",  hash, "Елена Козлова",
                "Организатор шахматного кружка",    Role.USER,  Level.AMATEUR);
        User admin      = user("admin@demo.local", hash, "Администратор",
                null,                               Role.ADMIN, Level.BEGINNER);

        userAlexey.setDemo(true);
        userMaria.setDemo(true);
        userOrg.setDemo(true);
        admin.setDemo(true);

        List<User> users = userRepo.saveAll(
                List.of(userAlexey, userMaria, userOrg, userOrg2, admin));
        userAlexey = users.get(0);
        userMaria  = users.get(1);
        userOrg    = users.get(2);
        userOrg2   = users.get(3);
        admin      = users.get(4);

        Sport football   = sport("Футбол",          "футбол",          SportCategory.TEAM,       "⚽",  false);
        Sport chess      = sport("Шахматы",          "шахматы",         SportCategory.INDIVIDUAL, "♟",  false);
        Sport running    = sport("Бег",              "бег",             SportCategory.INDIVIDUAL, "🏃",  false);
        Sport yoga       = sport("Йога",             "йога",            SportCategory.OTHER,      "🧘",  false);
        Sport basketball = sport("Баскетбол",        "баскетбол",       SportCategory.TEAM,       "🏀",  false);
        Sport charge     = sport("Зарядка",          "зарядка",         SportCategory.OTHER,      "💪",  false);
        Sport orienteer  = sport("Спортивное ориентирование", "спортивное ориентирование",
                SportCategory.INDIVIDUAL, "🧭",  false);
        Sport volleyball = sport("Волейбол",         "волейбол",        SportCategory.TEAM,       "🏐",  false);
        Sport tennis     = sport("Теннис",           "теннис",          SportCategory.INDIVIDUAL, "🎾",  false);
        Sport swimming   = sport("Плавание",         "плавание",        SportCategory.INDIVIDUAL, "🏊",  false);
        Sport cycling    = sport("Велосипед",        "велосипед",       SportCategory.INDIVIDUAL, "🚴",  false);
        Sport tableTennis= sport("Настольный теннис","настольный теннис",SportCategory.INDIVIDUAL,"🏓",  false);
        Sport slackline  = sport("Слэклайн",         "слэклайн",        SportCategory.OTHER,      "🪢",  true);

        List<Sport> sports = sportRepo.saveAll(List.of(
                football, chess, running, yoga, basketball, charge,
                orienteer, volleyball, tennis, swimming, cycling, tableTennis, slackline));

        football    = sports.get(0);
        chess       = sports.get(1);
        running     = sports.get(2);
        yoga        = sports.get(3);
        basketball  = sports.get(4);
        charge      = sports.get(5);
        orienteer   = sports.get(6);
        volleyball  = sports.get(7);
        tennis      = sports.get(8);
        swimming    = sports.get(9);
        cycling     = sports.get(10);
        tableTennis = sports.get(11);

        userAlexey.setSportIds(Set.of(football.getId(), running.getId(), basketball.getId()));
        userMaria .setSportIds(Set.of(yoga.getId(), chess.getId(), volleyball.getId()));
        userOrg   .setSportIds(Set.of(football.getId()));
        userRepo.saveAll(List.of(userAlexey, userMaria, userOrg));

        Organization orgApproved = org(userOrg.getId(),
                "ФК «Дворовые легенды»",
                "Любительский футбольный клуб. Играем каждую субботу, берём всех!",
                "Футбол, мини-футбол",
                OrganizationStatus.APPROVED, null, admin.getId());

        Organization orgPending = org(userOrg2.getId(),
                "Шахматный кружок «Гамбит»",
                "Собираемся по средам в кафе. Приходите, научим!",
                "Шахматы, шашки",
                OrganizationStatus.PENDING, null, null);

        Organization orgRejected = org(userOrg2.getId(),
                "Беговой клуб «Ультра»",
                "Профессиональные забеги",
                "Бег",
                OrganizationStatus.REJECTED,
                "Недостаточно информации о деятельности. Уточните программу мероприятий.",
                admin.getId());

        List<Organization> orgs = orgRepo.saveAll(List.of(orgApproved, orgPending, orgRejected));
        orgApproved = orgs.get(0);

        Instant now = Instant.now();

        Event evRun = event(userAlexey.getId(), null,
                "Утренняя пробежка в парке",
                "Бегаем в лёгком темпе, 5 км. Подходит для новичков!",
                running.getId(), now.plus(2, ChronoUnit.DAYS).plusSeconds(8 * 3600),
                "Красноярск", "Центральный парк, главный вход",
                JoinMode.OPEN, null, "Удобная спортивная обувь", Level.BEGINNER, EventStatus.PUBLISHED);

        Event evYoga = event(userMaria.getId(), null,
                "Утренняя йога на свежем воздухе",
                "Занятие для начинающих. Берите коврик и хорошее настроение!",
                yoga.getId(), now.plus(1, ChronoUnit.DAYS).plusSeconds(7 * 3600 + 1800),
                "Красноярск", "Набережная, рядом с фонтаном",
                JoinMode.OPEN, 15, null, Level.BEGINNER, EventStatus.PUBLISHED);

        Event evVolley = event(userMaria.getId(), null,
                "Пляжный волейбол на набережной",
                "Каждый вечер гоняем мяч на берегу. Новичков принимаем с удовольствием!",
                volleyball.getId(), now.plus(3, ChronoUnit.DAYS).plusSeconds(18 * 3600),
                "Красноярск", "Набережная, волейбольная площадка",
                JoinMode.OPEN, 12, null, Level.BEGINNER, EventStatus.PUBLISHED);

        Event evCharge = event(null, orgApproved.getId(),
                "Групповая зарядка в парке",
                "Бесплатная утренняя зарядка для всех желающих от нашего клуба.",
                charge.getId(), now.plus(4, ChronoUnit.DAYS).plusSeconds(7 * 3600),
                "Красноярск", "Центральный парк, спортивная площадка",
                JoinMode.OPEN, 30, null, Level.BEGINNER, EventStatus.PUBLISHED);

        Event evCycle = event(userAlexey.getId(), null,
                "Велопрогулка по набережной",
                "Неторопливый маршрут 15 км вдоль реки. Темп средний, остановки для фото.",
                cycling.getId(), now.plus(5, ChronoUnit.DAYS).plusSeconds(10 * 3600),
                "Красноярск", "Набережная, лестница у ж/д моста",
                JoinMode.OPEN, null, "Велосипед и шлем обязательны", Level.AMATEUR, EventStatus.PUBLISHED);

        Event evFootball = event(null, orgApproved.getId(),
                "Товарищеский матч по футболу",
                "Ищем команду соперника. 11 на 11, 2 тайма по 45 минут.",
                football.getId(), now.plus(3, ChronoUnit.DAYS).plusSeconds(19 * 3600),
                "Красноярск", "Стадион «Север», ул. Лесная 20",
                JoinMode.APPROVAL, 22, "Нужны бутсы или кроссовки. Форма предоставляется.", Level.AMATEUR, EventStatus.PUBLISHED);

        Event evOrient = event(userMaria.getId(), null,
                "Ночное ориентирование в лесу",
                "Для опытных участников. Карты и компас предоставляются.",
                orienteer.getId(), now.plus(5, ChronoUnit.DAYS).plusSeconds(21 * 3600),
                "Красноярск", "Торгашинский хребет, поляна старт",
                JoinMode.APPROVAL, 20, "Тёплая одежда, фонарик, физическая подготовка.", Level.PRO, EventStatus.PUBLISHED);

        Event evTennis = event(userAlexey.getId(), null,
                "Теннис: спарринг-партнёры",
                "Ищу партнёров для регулярных тренировок. Уровень: любитель и выше.",
                tennis.getId(), now.plus(6, ChronoUnit.DAYS).plusSeconds(17 * 3600),
                "Красноярск", "Теннисный корт «Динамо», ул. Спортивная 5",
                JoinMode.APPROVAL, 4, "Своя ракетка, опыт не менее 1 года", Level.AMATEUR, EventStatus.PUBLISHED);

        Event evSwim = event(null, orgApproved.getId(),
                "Утреннее плавание в бассейне",
                "Закрытые дорожки для нашей группы. Тренер по плаванию проведёт разминку.",
                swimming.getId(), now.plus(7, ChronoUnit.DAYS).plusSeconds(6 * 3600 + 1800),
                "Красноярск", "Бассейн «Дельфин», пр. Мира 10",
                JoinMode.APPROVAL, 10, "Шапочка и очки для плавания", Level.BEGINNER, EventStatus.PUBLISHED);

        Event evTableTennis = event(userMaria.getId(), null,
                "Турнир по настольному теннису",
                "Дружеский турнир в кафе-клубе. Ракетки есть, нужна только смелость!",
                tableTennis.getId(), now.plus(8, ChronoUnit.DAYS).plusSeconds(19 * 3600),
                "Красноярск", "Кафе-клуб «Пинг», ул. Ленина 30",
                JoinMode.APPROVAL, 16, null, Level.BEGINNER, EventStatus.PUBLISHED);

        Event evBasket = event(userAlexey.getId(), null,
                "Баскетбол 3×3",
                "Играем во дворе, нужны ещё двое.",
                basketball.getId(), now.plus(4, ChronoUnit.DAYS).plusSeconds(18 * 3600),
                "Красноярск", "Ул. Мира 10, дворовая площадка",
                JoinMode.OPEN, 6, null, Level.BEGINNER, EventStatus.UNPUBLISHED);
        evBasket.setModerationReason("Нарушение правил размещения: отсутствует контактная информация");
        evBasket.setModeratedBy(admin.getId());
        evBasket.setModeratedAt(now.minus(1, ChronoUnit.HOURS));

        List<Event> events = eventRepo.saveAll(List.of(
                evRun, evYoga, evVolley, evCharge, evCycle,
                evFootball, evOrient, evTennis, evSwim, evTableTennis,
                evBasket));

        evRun         = events.get(0);
        evYoga        = events.get(1);
        evVolley      = events.get(2);
        evCharge      = events.get(3);
        evCycle       = events.get(4);
        evFootball    = events.get(5);
        evOrient      = events.get(6);
        evTennis      = events.get(7);
        evSwim        = events.get(8);
        evTableTennis = events.get(9);
        evBasket      = events.get(10);

        partRepo.save(participation(evRun.getId(), userMaria.getId(),
                ParticipationStatus.CONFIRMED, null));

        partRepo.save(participation(evFootball.getId(), userAlexey.getId(),
                ParticipationStatus.PENDING, "Играю в полузащите, опыт 3 года"));

        Participation rejPart = participation(evFootball.getId(), userMaria.getId(),
                ParticipationStatus.REJECTED, "Хочу попробовать, я новичок");
        rejPart.setDecidedAt(now.minus(30, ChronoUnit.MINUTES));
        partRepo.save(rejPart);

        partRepo.save(participation(evTennis.getId(), userMaria.getId(),
                ParticipationStatus.PENDING, "Играю 2 года, есть своя ракетка"));

        partRepo.save(participation(evSwim.getId(), userAlexey.getId(),
                ParticipationStatus.PENDING, "Умею плавать, занимался в детстве"));

        partRepo.save(participation(evSwim.getId(), userMaria.getId(),
                ParticipationStatus.PENDING, "Хожу в бассейн 3 раза в неделю"));

        partRepo.save(participation(evTableTennis.getId(), userAlexey.getId(),
                ParticipationStatus.PENDING, "Первый раз участвую в турнире, но в пинг-понг играю давно"));

        commentRepo.saveAll(List.of(
                comment(evRun.getId(), userMaria.getId(),
                        "Можно прийти с собакой?", now.minus(5, ChronoUnit.HOURS)),
                comment(evRun.getId(), userAlexey.getId(),
                        "Конечно! Главное чтобы собака не сильно обгоняла 😄", now.minus(4, ChronoUnit.HOURS)),
                comment(evFootball.getId(), userAlexey.getId(),
                        "Будут ли запасные игроки на скамейке?", now.minus(3, ChronoUnit.HOURS)),
                comment(evVolley.getId(), userAlexey.getId(),
                        "Супер! Давно хотел попробовать пляжный волейбол. Форма нужна?", now.minus(5, ChronoUnit.HOURS)),
                comment(evVolley.getId(), userMaria.getId(),
                        "Форма не нужна, главное удобная одежда! Будем ждать.", now.minus(4, ChronoUnit.HOURS)),
                comment(evCycle.getId(), userMaria.getId(),
                        "Отличная идея! Можно взять велосипед напрокат?", now.minus(2, ChronoUnit.HOURS))
        ));

        notifRepo.saveAll(List.of(
                notif(userMaria.getId(), NotificationType.REGISTRATION_REJECTED,
                        "Заявка отклонена",
                        "Ваша заявка на «Товарищеский матч по футболу» отклонена организатором.",
                        evFootball.getId(), now.minus(30, ChronoUnit.MINUTES)),
                notif(userAlexey.getId(), NotificationType.EVENT_UNPUBLISHED,
                        "Событие снято с публикации",
                        "Ваше событие «Баскетбол 3×3» снято с публикации. Причина: нарушение правил.",
                        evBasket.getId(), now.minus(1, ChronoUnit.HOURS)),
                notif(userOrg.getId(), NotificationType.NEW_REGISTRATION,
                        "Новые заявки",
                        "На «Утреннее плавание в бассейне» подано 2 новые заявки.",
                        evSwim.getId(), now.minus(1, ChronoUnit.HOURS)),
                notif(userAlexey.getId(), NotificationType.NEW_REGISTRATION,
                        "Заявка на теннис",
                        "Мария Иванова подала заявку на «Теннис: спарринг-партнёры».",
                        evTennis.getId(), now.minus(3, ChronoUnit.HOURS))
        ));

        log.info("DataSeeder: готово. Пользователей: {}, видов спорта: {}, событий: {}",
                userRepo.count(), sportRepo.count(), eventRepo.count());
    }

    private User user(String email, String hash, String name, String about,
                      Role role, Level level) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(hash);
        u.setName(name);
        u.setAbout(about);
        u.setRole(role);
        u.setLevel(level);
        u.setActive(true);
        u.setCity("Красноярск");
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        return u;
    }

    private Sport sport(String name, String normalized, SportCategory category,
                        String icon, boolean custom) {
        Sport s = new Sport();
        s.setName(name);
        s.setNormalizedName(normalized);
        s.setCategory(category);
        s.setIcon(icon);
        s.setCustom(custom);
        s.setCreatedAt(Instant.now());
        return s;
    }

    private Organization org(Long ownerId, String name, String description,
                             String sportsDesc, OrganizationStatus status,
                             String rejectReason, Long decidedBy) {
        Organization o = new Organization();
        o.setOwnerId(ownerId);
        o.setName(name);
        o.setDescription(description);
        o.setSportsDescription(sportsDesc);
        o.setCity("Красноярск");
        o.setStatus(status);
        o.setRejectReason(rejectReason);
        o.setDecidedBy(decidedBy);
        o.setDecidedAt(decidedBy != null ? Instant.now().minus(2, ChronoUnit.DAYS) : null);
        o.setCreatedAt(Instant.now());
        o.setUpdatedAt(Instant.now());
        return o;
    }

    private Event event(Long userId, Long orgId, String title, String description,
                        Long sportId, Instant startsAt, String city, String address,
                        JoinMode joinMode, Integer capacity, String requirements,
                        Level level, EventStatus status) {
        Event e = new Event();
        e.setOrganizerUserId(userId);
        e.setOrganizerOrgId(orgId);
        e.setTitle(title);
        e.setDescription(description);
        e.setSportId(sportId);
        e.setStartsAt(startsAt);
        e.setCity(city);
        e.setAddress(address);
        e.setJoinMode(joinMode);
        e.setCapacity(capacity);
        e.setRequirements(requirements);
        e.setLevel(level);
        e.setStatus(status);
        e.setCreatedAt(Instant.now());
        e.setUpdatedAt(Instant.now());
        return e;
    }

    private Participation participation(Long eventId, Long userId,
                                        ParticipationStatus status, String message) {
        Participation p = new Participation();
        p.setEventId(eventId);
        p.setUserId(userId);
        p.setStatus(status);
        p.setMessage(message);
        p.setCreatedAt(Instant.now());
        p.setUpdatedAt(Instant.now());
        return p;
    }

    private Comment comment(Long eventId, Long authorId, String text, Instant createdAt) {
        Comment c = new Comment();
        c.setEventId(eventId);
        c.setAuthorId(authorId);
        c.setText(text);
        c.setCreatedAt(createdAt);
        c.setUpdatedAt(createdAt);
        return c;
    }

    private Notification notif(Long recipientId, NotificationType type,
                               String title, String body, Long eventId, Instant createdAt) {
        Notification n = new Notification();
        n.setRecipientId(recipientId);
        n.setType(type);
        n.setTitle(title);
        n.setBody(body);
        n.setEventId(eventId);
        n.setCreatedAt(createdAt);
        return n;
    }
}