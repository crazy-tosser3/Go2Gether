package com.example.STOLOTO;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.Models.Role;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.repository.SafetyBannerRepository;
import com.example.STOLOTO.repository.UserRepository;
import com.example.STOLOTO.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("SAFETY: Баннеры безопасности")
class SafetyTests {

    private static final String VALID_BANNER = """
            {
              "title": "Тестовый баннер",
              "category": "first_aid",
              "accent_color": "#123456",
              "slides": [
                {"title": "Шаг 1", "body": "Первый слайд"},
                {"title": "Шаг 2", "body": "Второй слайд"}
              ]
            }
            """;

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UserRepository users;
    @Autowired SafetyBannerRepository banners;

    // ---------- публичное чтение

    @Test
    @DisplayName("TC-070: GET /safety/banners без токена → 200, контент из V13")
    void feedNoToken_returns200() throws Exception {
        mvc.perform(get("/api/v1/safety/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(7))))
                .andExpect(jsonPath("$[0].category").value("first_aid"))
                .andExpect(jsonPath("$[0].accent_color").exists());
    }

    @Test
    @DisplayName("TC-071: GET /safety/banners/{id} → слайды и дисклеймер")
    void detailsNoToken_returnsSlidesAndDisclaimer() throws Exception {
        Long id = banners.findByPublishedTrueOrderBySortOrderAscIdAsc().get(0).getId();

        mvc.perform(get("/api/v1/safety/banners/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slides", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.slides[0].position").value(1))
                .andExpect(jsonPath("$.disclaimer").isNotEmpty());
    }

    @Test
    @DisplayName("TC-072: несуществующий баннер → 404")
    void unknownBanner_returns404() throws Exception {
        mvc.perform(get("/api/v1/safety/banners/999999"))
                .andExpect(status().isNotFound());
    }

    // ---------- права на админку

    @Test
    @DisplayName("TC-073: POST /admin/safety/banners без токена → 401")
    void anonymousCreate_returns401() throws Exception {
        mvc.perform(post("/api/v1/admin/safety/banners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BANNER))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC-074: USER создаёт баннер → 403")
    void userCreate_returns403() throws Exception {
        mvc.perform(post("/api/v1/admin/safety/banners")
                        .header("Authorization", "Bearer " + tokenFor(Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BANNER))
                .andExpect(status().isForbidden());
    }

    // ---------- админка

    @Test
    @DisplayName("TC-075: ADMIN создаёт баннер → 201, позиции слайдов 1..N")
    void adminCreate_returns201() throws Exception {
        mvc.perform(post("/api/v1/admin/safety/banners")
                        .header("Authorization", "Bearer " + tokenFor(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BANNER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.slides", hasSize(2)))
                .andExpect(jsonPath("$.slides[0].position").value(1))
                .andExpect(jsonPath("$.slides[1].position").value(2));
    }

    @Test
    @DisplayName("TC-076: ADMIN без слайдов → 422")
    void adminCreateWithoutSlides_returns422() throws Exception {
        mvc.perform(post("/api/v1/admin/safety/banners")
                        .header("Authorization", "Bearer " + tokenFor(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Без слайдов", "category": "first_aid", "slides": []}
                                """))
                .andExpect(status().isUnprocessableContent());
    }

    private String tokenFor(Role role) {
        User u = new User();
        u.setEmail("safety-" + System.nanoTime() + "@test.local");
        u.setPasswordHash("$2a$12$hfvmn6EMUMe208i6RsyG8u19Mik0euaAQw9Hpf.nk2FpiM9KXzwvS");
        u.setName("Test " + role);
        u.setRole(role);
        u.setActive(true);
        u.setCreatedAt(Instant.now());
        u.setUpdatedAt(Instant.now());
        users.saveAndFlush(u);
        return jwtService.issue(u);
    }
}