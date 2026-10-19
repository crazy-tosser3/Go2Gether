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
@RedisHash("participations")
public class Participation {
    @Id
    private String id;
    @Indexed
    private String eventId;
    @Indexed
    private String userId;
    private ParticipationStatus status;
    private String message;
    private String decisionReason;
    private Instant decidedAt;
    private Instant createdAt;
}
