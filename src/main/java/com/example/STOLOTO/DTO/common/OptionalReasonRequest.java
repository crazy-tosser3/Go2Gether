package com.example.STOLOTO.DTO.common;

import jakarta.validation.constraints.Size;

public record OptionalReasonRequest(@Size(max = 1000) String reason) {
}
