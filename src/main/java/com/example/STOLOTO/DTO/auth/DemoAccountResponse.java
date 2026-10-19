package com.example.STOLOTO.DTO.auth;

import com.example.STOLOTO.Models.Role;

public record DemoAccountResponse(String id, String name, String email, Role role, String description) {
}
