package com.example.STOLOTO.DTO.organization;

import com.example.STOLOTO.Models.OrganizationType;

import jakarta.validation.constraints.Size;

/** PATCH: null — поле не меняется. */
public record UpdateOrganizationRequest(
        @Size(min = 1, max = 200) String name,
        @Size(max = 5000) String description,
        OrganizationType type,
        @Size(max = 500) String sportsDescription,
        @Size(max = 100) String city,
        @Size(max = 500) String websiteUrl,
        @Size(max = 500) String avatarUrl,
        @Size(min = 1, max = 1000) String contacts) {
}
