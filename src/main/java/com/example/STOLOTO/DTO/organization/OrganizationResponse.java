package com.example.STOLOTO.DTO.organization;

import java.time.Instant;
import java.util.List;

import com.example.STOLOTO.DTO.sport.SportResponse;
import com.example.STOLOTO.DTO.user.UserShortResponse;
import com.example.STOLOTO.Models.OrganizationStatus;
import com.example.STOLOTO.Models.OrganizationType;

public record OrganizationResponse(
        String id,
        String name,
        String description,
        OrganizationType type,
        List<SportResponse> sports,
        String city,
        String contacts,
        OrganizationStatus status,
        String rejectReason,
        Instant reviewedAt,
        UserShortResponse owner,
        Instant createdAt,
        Instant updatedAt) {
}
