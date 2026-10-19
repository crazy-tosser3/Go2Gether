package com.example.STOLOTO.DTO.auth;

import java.io.Serializable;

import com.example.STOLOTO.Models.Role;

/** Serializable — кладётся в Redis-кэш. */
public record DemoAccountResponse(Long id, String name, String email, Role role, String description)
        implements Serializable {
}
