package com.example.STOLOTO.DTO.organization;

import com.example.STOLOTO.Models.OrganizationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 5000) String description,
        @NotNull OrganizationType type,
        @Size(max = 500) String sportsDescription,
        @Size(max = 100) String city,
        @Size(max = 500) String websiteUrl,
        @Size(max = 500) String avatarUrl,
        @NotBlank @Size(max = 1000) String contacts) {
}
