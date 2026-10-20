package com.example.STOLOTO.DTO.auth;

import java.util.List;

import com.example.STOLOTO.DTO.organization.OrganizationShortResponse;
import com.example.STOLOTO.DTO.user.UserResponse;

public record MeResponse(UserResponse user, List<OrganizationShortResponse> organizations) {
}
