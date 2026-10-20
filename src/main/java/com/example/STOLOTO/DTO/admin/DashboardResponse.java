package com.example.STOLOTO.DTO.admin;

public record DashboardResponse(
        long pendingOrganizations,
        long approvedOrganizations,
        long publishedEvents,
        long unpublishedEvents,
        long deletedEvents,
        long cancelledEvents,
        long hiddenComments,
        long pendingReports,
        long users) {
}
