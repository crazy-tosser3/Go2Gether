package com.example.STOLOTO.DTO.participation;

import com.example.STOLOTO.DTO.event.EventDetailsResponse;

public record JoinResponse(String message, ParticipationResponse participation, EventDetailsResponse event) {
}
