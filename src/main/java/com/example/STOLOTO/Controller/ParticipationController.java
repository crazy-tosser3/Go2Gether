package com.example.STOLOTO.Controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.common.OptionalReasonRequest;
import com.example.STOLOTO.DTO.participation.ParticipationResponse;
import com.example.STOLOTO.Services.ParticipationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/participations")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService participationService;

    @PostMapping("/{pid}/approve")
    public ParticipationResponse approve(@PathVariable Long pid) {
        return participationService.approve(pid);
    }

    @PostMapping("/{pid}/reject")
    public ParticipationResponse reject(@PathVariable Long pid,
            @Valid @RequestBody(required = false) OptionalReasonRequest request) {
        return participationService.reject(pid, request == null ? null : request.reason());
    }
}
