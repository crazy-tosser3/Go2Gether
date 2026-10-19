package com.example.STOLOTO.DTO.comment;

import java.time.Instant;

import com.example.STOLOTO.DTO.organization.OrganizationShortResponse;
import com.example.STOLOTO.DTO.user.UserShortResponse;

public record CommentResponse(
        Long id,
        Long eventId,
        Long parentId,
        UserShortResponse author,
        OrganizationShortResponse asOrganization,
        boolean isOrganizer,
        String text,
        boolean isHidden,
        String hiddenReason,
        boolean isEdited,
        Instant createdAt,
        Instant updatedAt) {
}
