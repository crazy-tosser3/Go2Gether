package com.example.STOLOTO.Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.admin.DashboardResponse;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.OrganizationStatus;
import com.example.STOLOTO.repository.CommentRepository;
import com.example.STOLOTO.repository.EventRepository;
import com.example.STOLOTO.repository.OrganizationRepository;
import com.example.STOLOTO.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminService {

    private final OrganizationRepository organizations;
    private final EventRepository events;
    private final CommentRepository comments;
    private final UserRepository users;

    public DashboardResponse dashboard() {
        return new DashboardResponse(
                organizations.countByStatus(OrganizationStatus.PENDING),
                organizations.countByStatus(OrganizationStatus.APPROVED),
                events.countByStatus(EventStatus.PUBLISHED),
                events.countByStatus(EventStatus.UNPUBLISHED),
                events.countByStatus(EventStatus.DELETED),
                events.countByStatus(EventStatus.CANCELLED),
                comments.countByHiddenTrue(),
                users.count());
    }
}
