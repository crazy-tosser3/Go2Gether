package com.example.STOLOTO.Models;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ровно одно из двух заполнено (CHECK chk_one_organizer). */
    private Long organizerUserId;
    private Long organizerOrgId;

    private String title;
    private String description;
    private Long sportId;

    private Instant startsAt;
    private Instant endsAt;
    private String city;
    private String address;
    private String locationNote;
    @Column(name = "latitude")
    private Double lat;
    @Column(name = "longitude")
    private Double lng;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private JoinMode joinMode = JoinMode.OPEN;
    private Integer capacity;
    @Enumerated(EnumType.STRING)
    private Level level;
    private String requirements;
    private String equipment;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private EventStatus status = EventStatus.PUBLISHED;
    private String cancelReason;
    private String moderationReason;
    private Long moderatedBy;
    private Instant moderatedAt;

    @Version
    private long version;

    private Instant createdAt;
    /** Меняется только при редактировании организатором — признак «изменено». */
    private Instant updatedAt;

    public OrganizerType organizerType() {
        return organizerOrgId != null ? OrganizerType.ORGANIZATION : OrganizerType.USER;
    }

    public boolean isPast() {
        return startsAt != null && !startsAt.isAfter(Instant.now());
    }

    public boolean isEdited() {
        return updatedAt != null && createdAt != null && updatedAt.isAfter(createdAt.plusSeconds(1));
    }

    /** Причина, по которой событие недоступно (для баннера и 410). */
    public String unavailableReason() {
        return status == EventStatus.CANCELLED ? cancelReason : moderationReason;
    }
}
