package com.example.STOLOTO.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.admin.AdminEventDetailsResponse;
import com.example.STOLOTO.DTO.admin.AdminOrganizationDetailsResponse;
import com.example.STOLOTO.DTO.admin.CacheStatsResponse;
import com.example.STOLOTO.DTO.admin.DashboardResponse;
import com.example.STOLOTO.DTO.admin.ModerationLogResponse;
import com.example.STOLOTO.DTO.comment.CommentResponse;
import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.DTO.common.OptionalReasonRequest;
import com.example.STOLOTO.DTO.common.ReasonRequest;
import com.example.STOLOTO.DTO.event.EventDetailsResponse;
import com.example.STOLOTO.DTO.event.EventShortResponse;
import com.example.STOLOTO.DTO.organization.OrganizationResponse;
import com.example.STOLOTO.DTO.report.ReportResponse;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.ModerationTargetType;
import com.example.STOLOTO.Models.OrganizationStatus;
import com.example.STOLOTO.Models.ReportStatus;
import com.example.STOLOTO.Services.AdminService;
import com.example.STOLOTO.Services.CommentService;
import com.example.STOLOTO.Services.EventService;
import com.example.STOLOTO.Services.AuditService;
import com.example.STOLOTO.Services.OrganizationService;
import com.example.STOLOTO.Services.ReportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final OrganizationService organizationService;
    private final EventService eventService;
    private final CommentService commentService;
    private final AuditService auditService;
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/organizations")
    public PageResponse<OrganizationResponse> organizations(
            @RequestParam(required = false) OrganizationStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return organizationService.adminList(status, q, page, size);
    }

    @GetMapping("/organizations/{id}")
    public AdminOrganizationDetailsResponse organization(@PathVariable Long id) {
        return organizationService.adminDetails(id);
    }

    @PostMapping("/organizations/{id}/approve")
    public OrganizationResponse approveOrganization(@PathVariable Long id) {
        return organizationService.approve(id);
    }

    @PostMapping("/organizations/{id}/reject")
    public OrganizationResponse rejectOrganization(@PathVariable Long id,
            @Valid @RequestBody ReasonRequest request) {
        return organizationService.reject(id, request.reason());
    }

    @GetMapping("/events")
    public PageResponse<EventShortResponse> events(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long organizer,
            @RequestParam(required = false) Long organization,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return eventService.adminSearch(status, q, organizer, organization, page, size);
    }

    @GetMapping("/events/{id}")
    public AdminEventDetailsResponse event(@PathVariable Long id) {
        return eventService.adminDetails(id);
    }

    @PostMapping("/events/{id}/unpublish")
    public EventDetailsResponse unpublish(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) {
        return eventService.unpublish(id, request.reason());
    }

    @DeleteMapping("/events/{id}")
    public EventDetailsResponse delete(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) {
        return eventService.delete(id, request.reason());
    }

    @PostMapping("/events/{id}/restore")
    public EventDetailsResponse restore(@PathVariable Long id) {
        return eventService.restore(id);
    }

    @PostMapping("/comments/{cid}/hide")
    public CommentResponse hideComment(@PathVariable Long cid, @Valid @RequestBody ReasonRequest request) {
        return commentService.hide(cid, request.reason());
    }

    @GetMapping("/reports")
    public PageResponse<ReportResponse> reports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(name = "event_id", required = false) Long eventId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return reportService.adminList(status, eventId, page, size);
    }

    @PostMapping("/reports/{id}/accept")
    public ReportResponse acceptReport(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) {
        return reportService.accept(id, request.reason());
    }

    @PostMapping("/reports/{id}/reject")
    public ReportResponse rejectReport(@PathVariable Long id,
            @Valid @RequestBody(required = false) OptionalReasonRequest request) {
        return reportService.reject(id, request == null ? null : request.reason());
    }

    @GetMapping("/audit-log")
    public PageResponse<ModerationLogResponse> auditLog(
            @RequestParam(name = "target_type", required = false) ModerationTargetType targetType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return auditService.list(targetType, page, size);
    }

    @GetMapping("/cache/stats")
    public CacheStatsResponse cacheStats() {
        return adminService.cacheStats();
    }

    @PostMapping("/cache/flush")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void flushCache() {
        adminService.flushCache();
    }
}
