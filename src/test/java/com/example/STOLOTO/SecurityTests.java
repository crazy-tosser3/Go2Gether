package com.example.STOLOTO;

import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.repository.UserRepository;
import com.example.STOLOTO.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WebAppConfiguration
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