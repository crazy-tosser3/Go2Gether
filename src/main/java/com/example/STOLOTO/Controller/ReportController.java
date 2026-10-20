package com.example.STOLOTO.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.report.CreateReportRequest;
import com.example.STOLOTO.DTO.report.ReportResponse;
import com.example.STOLOTO.Services.ReportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/events/{id}/reports")
    @ResponseStatus(HttpStatus.CREATED)
    public ReportResponse report(@PathVariable Long id, @Valid @RequestBody CreateReportRequest request) {
        return reportService.create(id, request);
    }

    @GetMapping("/users/me/reports")
    public List<ReportResponse> myReports() {
        return reportService.my();
    }
}
