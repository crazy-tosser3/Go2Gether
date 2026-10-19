package com.example.STOLOTO.DTO.admin;

import java.time.Instant;

import com.example.STOLOTO.DTO.user.UserShortResponse;
import com.example.STOLOTO.Models.AuditAction;
import com.example.STOLOTO.Models.ModerationTargetType;

public record ModerationLogResponse(
        Long id,
        UserShortResponse admin,
        AuditAction action,
        ModerationTargetType targetType,
        Long targetId,
        String reason,
        Instant createdAt) {
}
