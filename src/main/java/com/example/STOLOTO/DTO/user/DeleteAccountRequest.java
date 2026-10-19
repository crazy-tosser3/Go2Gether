package com.example.STOLOTO.DTO.user;

import jakarta.validation.constraints.NotBlank;

/** Подтверждение удаления аккаунта текущим паролем. */
public record DeleteAccountRequest(@NotBlank String password) {
}
