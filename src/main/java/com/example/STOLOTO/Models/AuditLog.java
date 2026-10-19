package com.example.STOLOTO.Models;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long adminId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AuditAction action;

    private Long targetOrgId;
    private Long targetEventId;
    private Long targetCommentId;

    private String reason;
    private Instant createdAt;

    public ModerationTargetType targetType() {
        if (targetOrgId != null) {
            return ModerationTargetType.ORGANIZATION;
        }
        return targetEventId != null ? ModerationTargetType.EVENT : ModerationTargetType.COMMENT;
    }

    public Long targetId() {
        if (targetOrgId != null) {
            return targetOrgId;
        }
        return targetEventId != null ? targetEventId : targetCommentId;
    }
}
