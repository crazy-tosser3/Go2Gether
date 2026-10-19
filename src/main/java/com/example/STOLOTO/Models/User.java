package com.example.STOLOTO.Models;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@RedisHash("users")
public class User {
    @Id
    private String id;
    private Role role = Role.USER;
    private String name;
    @Indexed
    private String email;
    private String passwordHash;
    private String avatarUrl;
    private String city;
    private String bio;
    private Level level;
    private List<String> sportIds = new ArrayList<>();
    private boolean demo;
    private Instant createdAt;
}
