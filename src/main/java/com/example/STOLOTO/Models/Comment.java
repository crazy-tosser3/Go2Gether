package com.example.STOLOTO.Models;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private Long authorId;
    private Long asOrganizationId;
    private Long parentId;
    private String text;

    @Column(name = "is_hidden")
    private boolean hidden;
    private Long hiddenBy;
    private Instant hiddenAt;
    private String hiddenReason;

    private Instant createdAt;
    private Instant updatedAt;
}
