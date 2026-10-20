package com.example.STOLOTO.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.STOLOTO.Models.EventReport;
import com.example.STOLOTO.Models.ReportStatus;

public interface EventReportRepository extends JpaRepository<EventReport, Long>, JpaSpecificationExecutor<EventReport> {
    boolean existsByEventIdAndReporterIdAndStatus(Long eventId, Long reporterId, ReportStatus status);

    List<EventReport> findByEventIdAndStatus(Long eventId, ReportStatus status);

    List<EventReport> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    long countByStatus(ReportStatus status);
}
