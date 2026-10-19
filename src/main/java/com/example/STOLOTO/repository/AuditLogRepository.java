package com.example.STOLOTO.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.STOLOTO.Models.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByTargetOrgIdIsNotNull(Pageable pageable);

    Page<AuditLog> findByTargetEventIdIsNotNull(Pageable pageable);

    Page<AuditLog> findByTargetCommentIdIsNotNull(Pageable pageable);
}
