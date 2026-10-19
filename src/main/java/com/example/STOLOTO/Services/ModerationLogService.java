package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.STOLOTO.DTO.admin.ModerationLogResponse;
import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.Models.ModerationAction;
import com.example.STOLOTO.Models.ModerationLog;
import com.example.STOLOTO.Models.ModerationTargetType;
import com.example.STOLOTO.repository.ModerationLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ModerationLogService {

    private final ModerationLogRepository logs;
    private final DtoMapper mapper;

    public void log(String adminId, ModerationTargetType targetType, String targetId, ModerationAction action,
            String reason) {
        ModerationLog l = new ModerationLog();
        l.setId(UUID.randomUUID().toString());
        l.setAdminId(adminId);
        l.setTargetType(targetType);
        l.setTargetId(targetId);
        l.setAction(action);
        l.setReason(reason);
        l.setCreatedAt(Instant.now());
        logs.save(l);
    }

    public PageResponse<ModerationLogResponse> list(ModerationTargetType targetType, Integer page, Integer size) {
        var items = logs.findAll().stream()
                .filter(l -> targetType == null || l.getTargetType() == targetType)
                .sorted(Comparator.comparing(ModerationLog::getCreatedAt).reversed())
                .map(mapper::toModerationLog)
                .toList();
        return PageResponse.of(items, page, size);
    }
}
