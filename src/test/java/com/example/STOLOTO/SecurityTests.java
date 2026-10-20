package com.example.STOLOTO;

import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.repository.UserRepository;
import com.example.STOLOTO.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import java.time.Instant;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("SEC: Проверка прав доступа")
class SecurityTests {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UserRepository users;

    @Test
    @DisplayName("TC-060: GET /auth/me без токена → 401")
    void anonymousMe_returns401() throws Exception {
        mvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC-060b: POST /events без токена → 401")
    void anonymousCreateEvent_returns401() throws Exception {
        mvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC-061: USER обращается к /admin/dashboard → 403")
    void userAccessesAdminDashboard_returns403() throws Exception {
        String token = tokenForUser(Role.USER);
        mvc.perform(get("/api/v1/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-061b: USER пытается одобрить организацию → 403")
    void userApprovesOrganization_returns403() throws Exception {
        String token = tokenForUser(Role.USER);
        mvc.perform(post("/api/v1/admin/organizations/1/approve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-061c: USER обращается к /admin/audit-log → 403")
    void userAccessesAuditLog_returns403() throws Exception {
        String token = tokenForUser(Role.USER);
        mvc.perform(get("/api/v1/admin/audit-log")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-062: USER смотрит /events/999/requests → 403 или 404")
    void userViewsRequestsOfAnotherEvent_notAllowed() throws Exception {
        String token = tokenForUser(Role.USER);
        mvc.perform(get("/api/v1/events/999/requests")
                        .header("Authorization", "Bearer " + token))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    if (status != 404 && status != 403) {
                        throw new AssertionError("Expected 403 or 404, got " + status);
                    }
                });
    }

    @Test
    @DisplayName("TC-063: GET /events без токена → 200")
    void publicEventsFeedNoToken_returns200() throws Exception {
        mvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-063b: GET /sports без токена → 200")
    void publicSportsNoToken_returns200() throws Exception {
        mvc.perform(get("/api/v1/sports"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("TC-064: GET /auth/demo-accounts без токена → 200")
    void demoAccountsNoToken_returns200() throws Exception {
        mvc.perform(get("/api/v1/auth/demo-accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("TC-065: запрос с мусорным токеном → 401, не 500")
    void invalidJwtToken_returns401NotServerError() throws Exception {
        mvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer this.is.not.a.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC-067: мусорная cookie на публичном запросе → 200 и cookie стирается")
    void garbageCookieOnPublic_returns200AndClearsCookie() throws Exception {
        mvc.perform(get("/api/v1/auth/demo-accounts")
                        .cookie(new Cookie("jwt", "garbage")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }

    @Test
    @DisplayName("TC-067b: мусорная cookie на закрытом запросе → 401 и cookie стирается")
    void garbageCookieOnProtected_returns401AndClearsCookie() throws Exception {
        mvc.perform(get("/api/v1/auth/me")
                        .cookie(new Cookie("jwt", "garbage")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }

    @Test
    @DisplayName("TC-068: регистрация без согласия на ПДн → 422 по полю consent")
    void registerWithoutConsent_returns422() throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Анна", "email": "anna-%d@test.local", "password": "Password123"}
                                """.formatted(System.nanoTime())))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error.fields.consent").exists());
    }

    @Test
    @DisplayName("TC-068b: регистрация с согласием → 201, время и версия сохранены")
    void registerWithConsent_savesConsent() throws Exception {
        String email = "anna-" + System.nanoTime() + "@test.local";
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Анна", "email": "%s", "password": "Password123", "consent": true}
                                """.formatted(email)))
                .andExpect(status().isCreated());

        User saved = users.findByEmailIgnoreCase(email).orElseThrow();
        org.junit.jupiter.api.Assertions.assertNotNull(saved.getConsentAt());
        org.junit.jupiter.api.Assertions.assertEquals("2026-10-19", saved.getConsentPolicyVersion());
    }

    @Autowired com.example.STOLOTO.repository.EventRepository eventsRepo;

    @Test
    @DisplayName("TC-069: владелец организации отвечает от своего имени → is_organizer = true")
    void orgOwnerCommentsPersonally_isMarkedAsOrganizer() throws Exception {
        Long eventId = eventsRepo.findAll().stream()
                .filter(e -> "Мини-футбол 5×5 вечером".equals(e.getTitle()))
                .findFirst().orElseThrow().getId();
        User owner = users.findByEmailIgnoreCase("org@demo.local").orElseThrow();

        mvc.perform(post("/api/v1/events/" + eventId + "/comments")
                        .header("Authorization", "Bearer " + jwtService.issue(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Ответ владельца клуба от своего имени\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.is_organizer").value(true))
                .andExpect(jsonPath("$.as_organization").doesNotExist());
    }

    @Autowired com.example.STOLOTO.repository.NotificationRepository notificationsRepo;

    @Test
    @DisplayName("TC-070: админ снял событие → записавшийся участник получил уведомление")
    void adminUnpublishes_participantGetsNotification() throws Exception {
        Long eventId = eventsRepo.findAll().stream()
                .filter(e -> "Мини-футбол 5×5 вечером".equals(e.getTitle()))
                .findFirst().orElseThrow().getId();
        Long participantId = users.findByEmailIgnoreCase("user@demo.local").orElseThrow().getId();

        mvc.perform(post("/api/v1/admin/events/" + eventId + "/unpublish")
                        .header("Authorization", "Bearer " + tokenForUser(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Тест: нарушение правил\"}"))
                .andExpect(status().isOk());

        boolean notified = notificationsRepo.findAll().stream()
                .anyMatch(n -> participantId.equals(n.getRecipientId())
                        && eventId.equals(n.getEventId())
                        && n.getType() == com.example.STOLOTO.Models.NotificationType.EVENT_UNPUBLISHED);
        org.junit.jupiter.api.Assertions.assertTrue(notified, "Участник должен получить уведомление о снятии");
    }

    @Test
    @DisplayName("TC-071: новый вопрос в обсуждении → организатор получил уведомление")
    void newRootComment_notifiesOrganizer() throws Exception {
        Long eventId = eventsRepo.findAll().stream()
                .filter(e -> "Городки: турнир для новичков".equals(e.getTitle()))
                .findFirst().orElseThrow().getId();
        User asker = users.findByEmailIgnoreCase("user@demo.local").orElseThrow();
        Long organizerId = users.findByEmailIgnoreCase("org2@demo.local").orElseThrow().getId();

        mvc.perform(post("/api/v1/events/" + eventId + "/comments")
                        .header("Authorization", "Bearer " + jwtService.issue(asker))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Во сколько начинается турнир?\"}"))
                .andExpect(status().isCreated());

        boolean notified = notificationsRepo.findAll().stream()
                .anyMatch(n -> organizerId.equals(n.getRecipientId())
                        && eventId.equals(n.getEventId())
                        && n.getType() == com.example.STOLOTO.Models.NotificationType.NEW_COMMENT);
        org.junit.jupiter.api.Assertions.assertTrue(notified, "Организатор должен получить уведомление о вопросе");
    }


    private String tokenForUser(Role role) {
        User u = new User();
        u.setEmail("test-" + System.nanoTime() + "@test.local");
        u.setPasswordHash("$2a$12$hfvmn6EMUMe208i6RsyG8u19Mik0euaAQw9Hpf.nk2FpiM9KXzwvS");
        u.setName("Test User");
        u.setRole(role);
        u.setActive(true);
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        users.saveAndFlush(u);
        return jwtService.issue(u);
    }
}