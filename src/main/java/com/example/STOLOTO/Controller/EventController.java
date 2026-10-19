package com.example.STOLOTO.Controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.comment.CommentResponse;
import com.example.STOLOTO.DTO.comment.CreateCommentRequest;
import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.DTO.common.ReasonRequest;
import com.example.STOLOTO.DTO.event.CreateEventRequest;
import com.example.STOLOTO.DTO.event.EventDetailsResponse;
import com.example.STOLOTO.DTO.event.EventShortResponse;
import com.example.STOLOTO.DTO.event.UpdateEventRequest;
import com.example.STOLOTO.DTO.participation.JoinRequest;
import com.example.STOLOTO.DTO.participation.JoinResponse;
import com.example.STOLOTO.DTO.participation.ParticipantResponse;
import com.example.STOLOTO.DTO.participation.ParticipantsResponse;
import com.example.STOLOTO.DTO.participation.ParticipationResponse;
import com.example.STOLOTO.Models.JoinMode;
import com.example.STOLOTO.Models.Level;
import com.example.STOLOTO.Models.ParticipationStatus;
import com.example.STOLOTO.Services.CommentService;
import com.example.STOLOTO.Services.EventService;
import com.example.STOLOTO.Services.ParticipationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final ParticipationService participationService;
    private final CommentService commentService;

    // события

    @GetMapping
    public PageResponse<EventShortResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long sport,
            @RequestParam(required = false) String city,
            @RequestParam(name = "date_from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(name = "date_to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Level level,
            @RequestParam(name = "join_mode", required = false) JoinMode joinMode,
            @RequestParam(required = false) Long organizer,
            @RequestParam(required = false) Long organization,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        var filter = new EventService.Filter(q, sport, city, dateFrom, dateTo, level, joinMode, organizer, organization);
        return eventService.search(filter, page, size);
    }

    @GetMapping("/{id}")
    public EventDetailsResponse get(@PathVariable Long id) {
        return eventService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDetailsResponse create(@Valid @RequestBody CreateEventRequest request) {
        return eventService.create(request);
    }

    @PatchMapping("/{id}")
    public EventDetailsResponse update(@PathVariable Long id, @Valid @RequestBody UpdateEventRequest request) {
        return eventService.update(id, request);
    }

    @PostMapping("/{id}/cancel")
    public EventDetailsResponse cancel(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) {
        return eventService.cancel(id, request.reason());
    }

    // запись и заявки

    @PostMapping("/{id}/join")
    @ResponseStatus(HttpStatus.CREATED)
    public JoinResponse join(@PathVariable Long id, @Valid @RequestBody(required = false) JoinRequest request) {
        return participationService.join(id, request == null ? null : request.message());
    }

    @DeleteMapping("/{id}/join")
    public ParticipationResponse leave(@PathVariable Long id) {
        return participationService.leave(id);
    }

    @GetMapping("/{id}/my-participation")
    public ParticipationResponse myParticipation(@PathVariable Long id) {
        return participationService.myParticipation(id);
    }

    @GetMapping("/{id}/participants")
    public ParticipantsResponse participants(@PathVariable Long id) {
        return participationService.participants(id);
    }

    @GetMapping("/{id}/requests")
    public List<ParticipantResponse> requests(@PathVariable Long id,
            @RequestParam(required = false) ParticipationStatus status) {
        return participationService.requests(id, status);
    }

    // обсуждение

    @GetMapping("/{id}/comments")
    public PageResponse<CommentResponse> comments(@PathVariable Long id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return commentService.list(id, page, size);
    }

    @PostMapping("/{id}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse comment(@PathVariable Long id, @Valid @RequestBody CreateCommentRequest request) {
        return commentService.create(id, request);
    }
}
