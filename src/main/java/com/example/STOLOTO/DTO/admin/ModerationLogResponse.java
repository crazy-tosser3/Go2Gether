package com.example.STOLOTO.DTO.admin;

import java.time.Instant;

import com.example.STOLOTO.DTO.user.UserShortResponse;
import com.example.STOLOTO.Models.ModerationAction;
import com.example.STOLOTO.Models.ModerationTargetType;

public record ModerationLogResponse(
        String id,
        UserShortResponse admin,
        ModerationTargetType targetType,
        String targetId,
        ModerationAction action,
        String reason,
        Instant createdAt) {
}
