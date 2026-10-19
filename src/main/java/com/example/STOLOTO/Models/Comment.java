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
@RedisHash("comments")
public class Comment {
    @Id
    private String id;
    @Indexed
    private String eventId;
    private String authorId;
    private String asOrganizationId;
    private String parentId;
    private String text;
    private boolean hidden;
    private String hiddenReason;
    private Instant createdAt;
    private Instant updatedAt;
}
