package com.example.STOLOTO.DTO.organization;

import java.util.List;

import com.example.STOLOTO.Models.OrganizationType;

import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
        @Size(min = 1, max = 150) String name,
        @Size(max = 5000) String description,
        OrganizationType type,
        @Size(max = 50) List<String> sportIds,
        @Size(max = 100) String city,
        @Size(min = 1, max = 1000) String contacts) {
}
