package com.example.STOLOTO.DTO.organization;

import java.util.List;

import com.example.STOLOTO.Models.OrganizationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 5000) String description,
        @NotNull OrganizationType type,
        @Size(max = 50) List<String> sportIds,
        @Size(max = 100) String city,
        @NotBlank @Size(max = 1000) String contacts) {
}
