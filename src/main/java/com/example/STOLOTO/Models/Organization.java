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
@RedisHash("organizations")
public class Organization {
    @Id
    private String id;
    @Indexed
    private String ownerId;
    private String name;
    private String description;
    private OrganizationType type;
    private List<String> sportIds = new ArrayList<>();
    private String city;
    private String contacts;
    @Indexed
    private OrganizationStatus status;
    private String rejectReason;
    private String reviewedBy;
    private Instant reviewedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
