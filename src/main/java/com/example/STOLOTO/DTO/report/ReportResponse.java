package com.example.STOLOTO.DTO.report;

import java.time.Instant;

import com.example.STOLOTO.DTO.event.EventShortResponse;
import com.example.STOLOTO.DTO.user.UserShortResponse;
import com.example.STOLOTO.Models.ReportReason;
import com.example.STOLOTO.Models.ReportStatus;

public record ReportResponse(
        Long id,
        EventShortResponse event,
        UserShortResponse reporter,
        ReportReason reason,
        String comment,
        ReportStatus status,
        String resolutionNote,
        Instant resolvedAt,
        Instant createdAt) {
}
