package com.example.STOLOTO.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;

public interface EventRepository extends ListCrudRepository<Event, String> {
    List<Event> findByStatus(EventStatus status);
    List<Event> findByOrganizerUserId(String organizerUserId);
    List<Event> findByOrganizerOrgId(String organizerOrgId);
    List<Event> findBySportIdAndStatus(String sportId, EventStatus status);
}
