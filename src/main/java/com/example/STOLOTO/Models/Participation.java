package com.example.STOLOTO.Models;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Запись на событие. UNIQUE(event_id, user_id): повторная запись переиспользует строку. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "registrations")
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private Long userId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ParticipationStatus status;
    private String message;
    private String decisionReason;
    private Instant decidedAt;
    private Integer waitlistPosition;

    private Instant createdAt;
    private Instant updatedAt;
}
