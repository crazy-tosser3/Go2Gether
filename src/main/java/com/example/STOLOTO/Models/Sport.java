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
@RedisHash("sports")
public class Sport {
    @Id
    private String id;
    private String name;
    @Indexed
    private String normalizedName;
    private SportCategory category;
    private String icon;
    private boolean custom;
    private String createdBy;
    private Instant createdAt;
}
