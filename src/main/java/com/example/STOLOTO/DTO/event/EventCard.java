package com.example.STOLOTO.DTO.event;

public record EventCard(EventDetailsResponse full, Long organizerUserId, Long organizerOwnerId) {

    public boolean isOrganizer(Long userId) {
        return userId != null && (userId.equals(organizerUserId) || userId.equals(organizerOwnerId));
    }
}
