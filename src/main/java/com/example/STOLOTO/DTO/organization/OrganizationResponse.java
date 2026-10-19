package com.example.STOLOTO.DTO.organization;

import java.time.Instant;

import com.example.STOLOTO.DTO.user.UserShortResponse;
import com.example.STOLOTO.Models.OrganizationStatus;
import com.example.STOLOTO.Models.OrganizationType;

public record OrganizationResponse(
        Long id,
        String name,
        String description,
        OrganizationType type,
        String sportsDescription,
        String city,
        String websiteUrl,
        String avatarUrl,
        String contacts,
        OrganizationStatus status,
        String rejectReason,
        Instant decidedAt,
        UserShortResponse owner,
        Instant createdAt,
        Instant updatedAt) {
}
