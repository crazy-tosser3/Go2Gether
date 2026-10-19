package com.example.STOLOTO.Services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.STOLOTO.DTO.admin.DashboardResponse;
import com.example.STOLOTO.Models.Comment;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.OrganizationStatus;
import com.example.STOLOTO.repository.CommentRepository;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final OrganizationRepository organizations;
    private final EventRepository events;
    private final CommentRepository comments;
    private final UserRepository users;

    public DashboardResponse dashboard() {
        var allEvents = events.findAll();
        return new DashboardResponse(
                organizations.findByStatus(OrganizationStatus.PENDING).size(),
                organizations.findByStatus(OrganizationStatus.APPROVED).size(),
                countEvents(allEvents, EventStatus.PUBLISHED),
                countEvents(allEvents, EventStatus.UNPUBLISHED),
                countEvents(allEvents, EventStatus.DELETED),
                countEvents(allEvents, EventStatus.CANCELLED),
                comments.findAll().stream().filter(Comment::isHidden).count(),
                users.count());
    }

    private static long countEvents(List<Event> all, EventStatus status) {
        return all.stream().filter(e -> e.getStatus() == status).count();
    }
}
