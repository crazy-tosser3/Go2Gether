package com.example.STOLOTO.DTO.participation;

import jakarta.validation.constraints.Size;

public record JoinRequest(@Size(max = 1000) String message) {
}
