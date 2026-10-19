package com.example.STOLOTO.DTO.organization;

import com.example.STOLOTO.Models.OrganizationStatus;
import com.example.STOLOTO.Models.OrganizationType;

public record OrganizationShortResponse(
        Long id,
        String name,
        OrganizationType type,
        String city,
        OrganizationStatus status) {
}
