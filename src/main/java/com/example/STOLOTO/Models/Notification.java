package com.example.STOLOTO.Models;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RedisHash("notifications")
public class Notification {
    @Id
    private String id;
    @Indexed
    private String userId;
    private NotificationType type;
    private Map<String, String> payload = new HashMap<>();
    private boolean read;
    private Instant createdAt;
}
