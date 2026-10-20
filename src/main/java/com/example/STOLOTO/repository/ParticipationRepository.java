package com.example.STOLOTO.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.STOLOTO.Models.Participation;
import com.example.STOLOTO.Models.ParticipationStatus;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    Optional<Participation> findByEventIdAndUserId(Long eventId, Long userId);

    List<Participation> findByEventIdAndStatusOrderByCreatedAtAsc(Long eventId, ParticipationStatus status);

    List<Participation> findByEventIdOrderByCreatedAtAsc(Long eventId);

    List<Participation> findByEventIdAndStatusInOrderByCreatedAtAsc(Long eventId,
            Collection<ParticipationStatus> statuses);

    List<Participation> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByEventIdAndStatus(Long eventId, ParticipationStatus status);

    Optional<Participation> findFirstByEventIdAndStatusOrderByWaitlistPositionAscCreatedAtAsc(Long eventId,
            ParticipationStatus status);

    @Query("select coalesce(max(p.waitlistPosition), 0) from Participation p where p.eventId = :eventId")
    int maxWaitlistPosition(Long eventId);

    List<Participation> findByEventIdInAndStatusIn(Collection<Long> eventIds,
            Collection<ParticipationStatus> statuses);
}
