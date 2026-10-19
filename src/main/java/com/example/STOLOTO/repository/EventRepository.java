package com.example.STOLOTO.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;

import jakarta.persistence.LockModeType;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    /** Блокировка строки события при записи/подтверждении — защита лимита мест от гонок. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Event e where e.id = :id")
    Optional<Event> findByIdForUpdate(Long id);

    List<Event> findByOrganizerUserIdOrderByStartsAtDesc(Long organizerUserId);

    List<Event> findByOrganizerUserIdAndStatusOrderByStartsAtDesc(Long organizerUserId, EventStatus status);

    List<Event> findByOrganizerOrgIdOrderByStartsAtDesc(Long organizerOrgId);

    List<Event> findByOrganizerOrgIdAndStatusOrderByStartsAtAsc(Long organizerOrgId, EventStatus status);

    List<Event> findByOrganizerUserIdOrOrganizerOrgIdInOrderByStartsAtDesc(Long organizerUserId,
            Collection<Long> organizerOrgIds);

    long countBySportIdAndStatusAndStartsAtAfter(Long sportId, EventStatus status, Instant after);

    long countByStatus(EventStatus status);
}
