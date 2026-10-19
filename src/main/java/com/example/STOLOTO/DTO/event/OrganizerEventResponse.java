package com.example.STOLOTO.DTO.event;

public record OrganizerEventResponse(EventShortResponse event, long pendingCount, long waitlistedCount) {
}
