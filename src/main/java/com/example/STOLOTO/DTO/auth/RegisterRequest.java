package com.example.STOLOTO.DTO.auth;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 128) String password,
        @NotNull(message = "Необходимо согласие на обработку персональных данных")
        @AssertTrue(message = "Необходимо согласие на обработку персональных данных")
        Boolean consent) {
}