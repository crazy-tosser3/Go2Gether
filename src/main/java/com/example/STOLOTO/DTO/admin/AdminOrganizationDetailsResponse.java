package com.example.STOLOTO.DTO.admin;

import java.util.List;

import com.example.STOLOTO.DTO.event.EventShortResponse;
import com.example.STOLOTO.DTO.organization.OrganizationResponse;
import com.example.STOLOTO.DTO.user.UserResponse;

public record AdminOrganizationDetailsResponse(
        OrganizationResponse organization,
        UserResponse owner,
        List<EventShortResponse> events) {
}
