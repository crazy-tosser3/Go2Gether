package com.example.STOLOTO.Controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.admin.AdminEventDetailsResponse;
import com.example.STOLOTO.DTO.admin.AdminOrganizationDetailsResponse;
import com.example.STOLOTO.DTO.admin.DashboardResponse;
import com.example.STOLOTO.DTO.admin.ModerationLogResponse;
import com.example.STOLOTO.DTO.comment.CommentResponse;
import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.DTO.common.ReasonRequest;
import com.example.STOLOTO.DTO.event.EventDetailsResponse;
import com.example.STOLOTO.DTO.event.EventShortResponse;
import com.example.STOLOTO.DTO.organization.OrganizationResponse;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.ModerationTargetType;
import com.example.STOLOTO.Models.OrganizationStatus;
import com.example.STOLOTO.Services.AdminService;
import com.example.STOLOTO.Services.CommentService;
import com.example.STOLOTO.Services.EventService;
import com.example.STOLOTO.Services.ModerationLogService;
import com.example.STOLOTO.Services.OrganizationService;

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
    private final ModerationLogService moderationLogService;

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return adminService.dashboard();
    }

    // организации

    @GetMapping("/organizations")
    public PageResponse<OrganizationResponse> organizations(
            @RequestParam(required = false) OrganizationStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return organizationService.adminList(status, q, page, size);
    }

    @GetMapping("/organizations/{id}")
    public AdminOrganizationDetailsResponse organization(@PathVariable String id) {
        return organizationService.adminDetails(id);
    }

    @PostMapping("/organizations/{id}/approve")
    public OrganizationResponse approveOrganization(@PathVariable String id) {
        return organizationService.approve(id);
    }

    @PostMapping("/organizations/{id}/reject")
    public OrganizationResponse rejectOrganization(@PathVariable String id,
            @Valid @RequestBody ReasonRequest request) {
        return organizationService.reject(id, request.reason());
    }

    // события

    @GetMapping("/events")
    public PageResponse<EventShortResponse> events(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String organizer,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return eventService.adminSearch(status, q, organizer, page, size);
    }

    @GetMapping("/events/{id}")
    public AdminEventDetailsResponse event(@PathVariable String id) {
        return eventService.adminDetails(id);
    }

    @PostMapping("/events/{id}/unpublish")
    public EventDetailsResponse unpublish(@PathVariable String id, @Valid @RequestBody ReasonRequest request) {
        return eventService.unpublish(id, request.reason());
    }

    @DeleteMapping("/events/{id}")
    public EventDetailsResponse delete(@PathVariable String id, @Valid @RequestBody ReasonRequest request) {
        return eventService.delete(id, request.reason());
    }

    @PostMapping("/events/{id}/restore")
    public EventDetailsResponse restore(@PathVariable String id) {
        return eventService.restore(id);
    }

    // комментарии и журнал

    @PostMapping("/comments/{cid}/hide")
    public CommentResponse hideComment(@PathVariable String cid, @Valid @RequestBody ReasonRequest request) {
        return commentService.hide(cid, request.reason());
    }

    @GetMapping("/audit-log")
    public PageResponse<ModerationLogResponse> auditLog(
            @RequestParam(name = "target_type", required = false) ModerationTargetType targetType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return moderationLogService.list(targetType, page, size);
    }
}
