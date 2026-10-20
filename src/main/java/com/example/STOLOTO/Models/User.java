package com.example.STOLOTO.Models;

import java.time.Instant;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String passwordHash;
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Role role = Role.USER;

    private String about;
    private String city;
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "fitness_level")
    private Level level;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_sports", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "sport_id")
    private Set<Long> sportIds = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_sport_levels", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "sport_id")
    @Enumerated(EnumType.STRING)
    @Column(name = "level")
    private Map<Long, Level> sportLevels = new LinkedHashMap<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preferred_times", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot")
    private Set<TimeSlot> preferredTimes = EnumSet.noneOf(TimeSlot.class);

    private Integer birthYear;
    private Integer radiusKm;
    private Integer preferredAgeFrom;
    private Integer preferredAgeTo;

    @Column(name = "is_active")
    private boolean active = true;
    @Column(name = "is_demo")
    private boolean demo;

    private Instant createdAt;
    private Instant updatedAt;

    private Instant consentAt;
    private String consentPolicyVersion;
}
