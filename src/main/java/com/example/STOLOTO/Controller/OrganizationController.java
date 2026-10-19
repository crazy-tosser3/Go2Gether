package com.example.STOLOTO.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.DTO.event.EventShortResponse;
import com.example.STOLOTO.DTO.organization.CreateOrganizationRequest;
import com.example.STOLOTO.DTO.organization.OrganizationResponse;
import com.example.STOLOTO.DTO.organization.UpdateOrganizationRequest;
import com.example.STOLOTO.Services.OrganizationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationResponse create(@Valid @RequestBody CreateOrganizationRequest request) {
        return organizationService.create(request);
    }

    @GetMapping("/my")
    public List<OrganizationResponse> my() {
        return organizationService.my();
    }

    @PatchMapping("/{id}")
    public OrganizationResponse update(@PathVariable Long id, @Valid @RequestBody UpdateOrganizationRequest request) {
        return organizationService.update(id, request);
    }

    @PostMapping("/{id}/resubmit")
    public OrganizationResponse resubmit(@PathVariable Long id) {
        return organizationService.resubmit(id);
    }

    @GetMapping
    public PageResponse<OrganizationResponse> catalog(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String sport,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return organizationService.catalog(q, sport, city, page, size);
    }

    @GetMapping("/{id}")
    public OrganizationResponse get(@PathVariable Long id) {
        return organizationService.get(id);
    }

    @GetMapping("/{id}/events")
    public List<EventShortResponse> events(@PathVariable Long id) {
        return organizationService.events(id);
    }
}
