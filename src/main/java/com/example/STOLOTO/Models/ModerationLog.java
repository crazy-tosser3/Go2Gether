package com.example.STOLOTO.Models;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RedisHash("moderation_logs")
public class ModerationLog {
    @Id
    private String id;
    private String adminId;
    private ModerationTargetType targetType;
    private String targetId;
    private ModerationAction action;
    private String reason;
    private Instant createdAt;
}
