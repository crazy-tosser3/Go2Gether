package com.example.STOLOTO.Services;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.admin.ModerationLogResponse;
import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.Models.AuditAction;
import com.example.STOLOTO.Models.AuditLog;
import com.example.STOLOTO.Models.ModerationTargetType;
import com.example.STOLOTO.repository.AuditLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository logs;
    private final DtoMapper mapper;

    public void organization(Long adminId, Long orgId, AuditAction action, String reason) {
        AuditLog l = entry(adminId, action, reason);
        l.setTargetOrgId(orgId);
        logs.save(l);
    }

    public void event(Long adminId, Long eventId, AuditAction action, String reason) {
        AuditLog l = entry(adminId, action, reason);
        l.setTargetEventId(eventId);
        logs.save(l);
    }

    public void comment(Long adminId, Long commentId, AuditAction action, String reason) {
        AuditLog l = entry(adminId, action, reason);
        l.setTargetCommentId(commentId);
        logs.save(l);
    }

    @Transactional(readOnly = true)
    public PageResponse<ModerationLogResponse> list(ModerationTargetType targetType, Integer page, Integer size) {
        var pageable = PageResponse.pageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> result = targetType == null ? logs.findAll(pageable) : switch (targetType) {
            case ORGANIZATION -> logs.findByTargetOrgIdIsNotNull(pageable);
            case EVENT -> logs.findByTargetEventIdIsNotNull(pageable);
            case COMMENT -> logs.findByTargetCommentIdIsNotNull(pageable);
        };
        return PageResponse.of(result, mapper::toModerationLog);
    }

    private static AuditLog entry(Long adminId, AuditAction action, String reason) {
        AuditLog l = new AuditLog();
        l.setAdminId(adminId);
        l.setAction(action);
        l.setReason(reason);
        l.setCreatedAt(Instant.now());
        return l;
    }
}
