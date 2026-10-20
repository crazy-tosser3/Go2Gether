package com.example.STOLOTO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.Models.AuditAction;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.NotificationType;
import com.example.STOLOTO.Models.ReportStatus;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.repository.AuditLogRepository;
import com.example.STOLOTO.repository.EventReportRepository;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.NotificationRepository;
import com.example.STOLOTO.repository.UserRepository;
import com.example.STOLOTO.security.JwtService;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("REPORT: Жалобы на мероприятия")
class ReportTests {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UserRepository users;
    @Autowired EventRepository events;
    @Autowired EventReportRepository reports;
    @Autowired NotificationRepository notifications;
    @Autowired AuditLogRepository auditLog;
    @Autowired JsonMapper json;

    @Test
    @DisplayName("TC-080: жалоба без входа → 401")
    void anonymousReport_returns401() throws Exception {
        mvc.perform(post("/api/v1/events/" + eventId("Шахматный блиц в сквере") + "/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"spam\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("TC-081: жалоба → 201 pending, повторная до решения → 409 ALREADY_REPORTED")
    void reportTwice_secondIsConflict() throws Exception {
        Long eventId = eventId("Шахматный блиц в сквере");
        String body = "{\"reason\": \"misleading\", \"comment\": \"Сквер закрыт\"}";
        mvc.perform(post("/api/v1/events/" + eventId + "/reports")
                        .header("Authorization", bearer("user@demo.local"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.reason").value("misleading"))
                .andExpect(jsonPath("$.event.id").value(eventId));
        mvc.perform(post("/api/v1/events/" + eventId + "/reports")
                        .header("Authorization", bearer("user@demo.local"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ALREADY_REPORTED"));
    }

    @Test
    @DisplayName("TC-082: жалоба на своё мероприятие → 409 OWN_EVENT; other без комментария → 422")
    void ownEventAndOtherWithoutComment() throws Exception {
        mvc.perform(post("/api/v1/events/" + eventId("Шахматный блиц в сквере") + "/reports")
                        .header("Authorization", bearer("org2@demo.local"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"spam\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("OWN_EVENT"));
        mvc.perform(post("/api/v1/events/" + eventId("Шахматный блиц в сквере") + "/reports")
                        .header("Authorization", bearer("user@demo.local"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"other\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.error.fields.comment").exists());
    }

    @Test
    @DisplayName("TC-083: жалоба на снятое мероприятие → 410; USER в /admin/reports → 403")
    void unpublishedEventAndUserAccessToQueue() throws Exception {
        mvc.perform(post("/api/v1/events/" + eventId("Баскетбол 3×3") + "/reports")
                        .header("Authorization", bearer("user2@demo.local"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"spam\"}"))
                .andExpect(status().isGone());
        mvc.perform(get("/api/v1/admin/reports").header("Authorization", bearer("user@demo.local")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-084: админ принимает жалобу → мероприятие снято, все жалобы на него приняты, авторы уведомлены")
    void acceptReport_unpublishesEventAndClosesAllReports() throws Exception {
        Long eventId = eventId("Зарядка во дворе");
        Long seeded = reports.findByEventIdAndStatus(eventId, ReportStatus.PENDING).getFirst().getId();
        Long second = createReport(eventId, "org@demo.local");

        mvc.perform(post("/api/v1/admin/reports/" + seeded + "/accept")
                        .header("Authorization", bearer("admin@demo.local"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"Адрес неактуален\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("accepted"))
                .andExpect(jsonPath("$.resolution_note").value("Адрес неактуален"));

        assertEquals(EventStatus.UNPUBLISHED, events.findById(eventId).orElseThrow().getStatus());
        assertEquals(ReportStatus.ACCEPTED, reports.findById(second).orElseThrow().getStatus());
        Long secondAuthor = users.findByEmailIgnoreCase("org@demo.local").orElseThrow().getId();
        assertTrue(notifications.findAll().stream().anyMatch(n -> secondAuthor.equals(n.getRecipientId())
                && n.getType() == NotificationType.REPORT_RESOLVED && eventId.equals(n.getEventId())));
    }

    @Test
    @DisplayName("TC-085: админ отклоняет жалобу → мероприятие остаётся, запись в журнале, повторное решение → 409")
    void rejectReport_keepsEventAndWritesAudit() throws Exception {
        Long eventId = eventId("Зарядка во дворе");
        Long seeded = reports.findByEventIdAndStatus(eventId, ReportStatus.PENDING).getFirst().getId();

        mvc.perform(post("/api/v1/admin/reports/" + seeded + "/reject")
                        .header("Authorization", bearer("admin@demo.local"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"Стройка закончилась\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("rejected"));
        mvc.perform(post("/api/v1/admin/reports/" + seeded + "/reject")
                        .header("Authorization", bearer("admin@demo.local")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ALREADY_REVIEWED"));

        assertEquals(EventStatus.PUBLISHED, events.findById(eventId).orElseThrow().getStatus());
        assertTrue(auditLog.findAll().stream().anyMatch(l -> l.getAction() == AuditAction.REPORT_REJECTED
                && eventId.equals(l.getTargetEventId())));
        mvc.perform(get("/api/v1/users/me/reports").header("Authorization", bearer("user@demo.local")))
                .andExpect(jsonPath("$[?(@.id == " + seeded + ")].status").value("rejected"));
    }

    private Long createReport(Long eventId, String email) throws Exception {
        String response = mvc.perform(post("/api/v1/events/" + eventId + "/reports")
                        .header("Authorization", bearer(email))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\": \"dangerous\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("id").asLong();
    }

    private Long eventId(String title) {
        return events.findAll().stream().filter(e -> title.equals(e.getTitle())).findFirst().orElseThrow().getId();
    }

    private String bearer(String email) {
        User u = users.findByEmailIgnoreCase(email).orElseThrow();
        return "Bearer " + jwtService.issue(u);
    }
}
