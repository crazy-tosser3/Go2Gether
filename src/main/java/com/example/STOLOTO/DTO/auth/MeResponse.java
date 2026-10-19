package com.example.STOLOTO.DTO.auth;

import java.util.List;

import com.example.STOLOTO.DTO.organization.OrganizationShortResponse;
import com.example.STOLOTO.DTO.user.UserResponse;

/** Текущий пользователь, роль, его организации и их статусы. */
public record MeResponse(UserResponse user, List<OrganizationShortResponse> organizations) {
}
