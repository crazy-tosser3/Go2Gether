package com.example.STOLOTO.DTO.participation;

import java.util.List;

public record ParticipantsResponse(long count, Integer capacity, List<ParticipantResponse> participants) {
}
