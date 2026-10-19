package com.example.STOLOTO.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import com.example.STOLOTO.Models.Participation;

public interface ParticipationRepository extends ListCrudRepository<Participation, String> {
    List<Participation> findByEventId(String eventId);
    List<Participation> findByUserId(String userId);
    List<Participation> findByEventIdAndUserId(String eventId, String userId);
}
