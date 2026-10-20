package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.DTO.report.CreateReportRequest;
import com.example.STOLOTO.DTO.report.ReportResponse;
import com.example.STOLOTO.Models.AuditAction;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventReport;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.NotificationType;
import com.example.STOLOTO.Models.ReportReason;
import com.example.STOLOTO.Models.ReportStatus;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.repository.EventReportRepository;
import com.example.STOLOTO.security.CurrentUser;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final EventReportRepository reports;
    private final EventService eventService;
    private final NotificationService notificationService;
    private final AuditService audit;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    public ReportResponse create(Long eventId, CreateReportRequest r) {
        User me = currentUser.require();
        Event e = eventService.require(eventId);
        if (e.getStatus() != EventStatus.PUBLISHED) {
            throw EventService.unavailable(e);
        }
        if (mapper.isOrganizer(e, me.getId())) {
            throw ApiException.conflict("OWN_EVENT", "Нельзя пожаловаться на своё мероприятие");
        }
        String comment = UserService.blankToNull(r.comment());
        if (r.reason() == ReportReason.OTHER && comment == null) {
            throw ApiException.validation("comment", "Опишите проблему, если выбрана причина other");
        }
        if (reports.existsByEventIdAndReporterIdAndStatus(eventId, me.getId(), ReportStatus.PENDING)) {
            throw alreadyReported();
        }
        EventReport report = new EventReport();
        report.setEventId(eventId);
        report.setReporterId(me.getId());
        report.setReason(r.reason());
        report.setComment(comment);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(Instant.now());
        try {
            reports.saveAndFlush(report);
        } catch (DataIntegrityViolationException ex) {
            // параллельный двойной клик упирается в uq_event_reports_pending
            throw alreadyReported();
        }
        return mapper.toReport(report, e);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> my() {
        Long me = currentUser.require().getId();
        return reports.findByReporterIdOrderByCreatedAtDesc(me).stream()
                .map(r -> mapper.toReport(r, eventService.require(r.getEventId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportResponse> adminList(ReportStatus status, Long eventId, Integer page, Integer size) {
        Specification<EventReport> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (status != null) {
                p.add(cb.equal(root.get("status"), status));
            }
            if (eventId != null) {
                p.add(cb.equal(root.get("eventId"), eventId));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
        var pageable = PageResponse.pageable(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return PageResponse.of(reports.findAll(spec, pageable),
                r -> mapper.toReport(r, eventService.require(r.getEventId())));
    }

    /** Принять жалобу = снять мероприятие с публикации; остальные жалобы на него закрываются вместе с ней. */
    public ReportResponse accept(Long reportId, String reason) {
        User admin = currentUser.require();
        EventReport r = requirePending(reportId);
        Event e = eventService.require(r.getEventId());
        if (e.getStatus() == EventStatus.PUBLISHED) {
            eventService.unpublish(e.getId(), reason);
        } else {
            // мероприятие уже снято, отменено или удалено — фиксируем решение по жалобе
            r.setStatus(ReportStatus.ACCEPTED);
            r.setResolutionNote(reason);
            r.setResolvedBy(admin.getId());
            r.setResolvedAt(Instant.now());
            notificationService.notify(r.getReporterId(), NotificationType.REPORT_RESOLVED, "Жалоба принята",
                    "«" + e.getTitle() + "» уже недоступно для записи", e.getId());
        }
        return mapper.toReport(r, e);
    }

    public ReportResponse reject(Long reportId, String reason) {
        User admin = currentUser.require();
        EventReport r = requirePending(reportId);
        Event e = eventService.require(r.getEventId());
        r.setStatus(ReportStatus.REJECTED);
        r.setResolutionNote(reason);
        r.setResolvedBy(admin.getId());
        r.setResolvedAt(Instant.now());
        audit.event(admin.getId(), e.getId(), AuditAction.REPORT_REJECTED, reason);
        notificationService.notify(r.getReporterId(), NotificationType.REPORT_RESOLVED, "Жалоба отклонена",
                "Жалоба на «" + e.getTitle() + "» рассмотрена, нарушений не найдено"
                        + (reason == null ? "" : ": " + reason),
                e.getId());
        return mapper.toReport(r, e);
    }

    private EventReport requirePending(Long id) {
        EventReport r = reports.findById(id).orElseThrow(() -> ApiException.notFound("Жалоба не найдена"));
        if (r.getStatus() != ReportStatus.PENDING) {
            throw ApiException.conflict("ALREADY_REVIEWED", "Жалоба уже рассмотрена",
                    Map.of("status", r.getStatus().name().toLowerCase()));
        }
        return r;
    }

    private static ApiException alreadyReported() {
        return ApiException.conflict("ALREADY_REPORTED", "Ваша жалоба на это мероприятие уже на рассмотрении");
    }
}
