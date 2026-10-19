package com.example.STOLOTO.DTO.admin;

import java.time.Instant;
import java.util.List;

import com.example.STOLOTO.DTO.comment.CommentResponse;
import com.example.STOLOTO.DTO.event.EventDetailsResponse;
import com.example.STOLOTO.DTO.participation.ParticipantResponse;
import com.example.STOLOTO.DTO.user.UserShortResponse;

public record AdminEventDetailsResponse(
        EventDetailsResponse event,
        UserShortResponse moderatedBy,
        Instant moderatedAt,
        List<ParticipantResponse> participations,
        List<CommentResponse> comments) {
}
