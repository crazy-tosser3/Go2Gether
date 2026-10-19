package com.example.STOLOTO.Models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RedisHash("events")
public class Event {
    @Id
    private String id;
    private String title;
    private String description;
    @Indexed
    private String sportId;
    private OrganizerType organizerType;
    @Indexed
    private String organizerUserId;
    @Indexed
    private String organizerOrgId;
    private Instant startsAt;
    private Instant endsAt;
    private String city;
    private String address;
    private String locationNote;
    private Double lat;
    private Double lng;
    private Level level;
    private String requirements;
    private String equipment;
    private JoinMode joinMode;
    private Integer capacity;
    @Indexed
    private EventStatus status;
    private String cancelReason;
    private String moderationReason;
    private String moderatedBy;
    private Instant moderatedAt;
    private Instant createdAt;
    /** Заполняется только при редактировании организатором — признак «изменено». */
    private Instant updatedAt;

    public boolean isPast() {
        return startsAt != null && !startsAt.isAfter(Instant.now());
    }

    /** Причина, по которой событие недоступно (для баннера и 410). */
    public String unavailableReason() {
        return status == EventStatus.CANCELLED ? cancelReason : moderationReason;
    }
}
