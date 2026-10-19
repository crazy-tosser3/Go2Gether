package com.example.STOLOTO.Services;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.STOLOTO.DTO.comment.CommentResponse;
import com.example.STOLOTO.DTO.comment.CreateCommentRequest;
import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.Models.Comment;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.ModerationAction;
import com.example.STOLOTO.Models.ModerationTargetType;
import com.example.STOLOTO.Models.NotificationType;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.repository.CommentRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository comments;
    private final EventService eventService;
    private final OrganizationService organizationService;
    private final NotificationService notificationService;
    private final ModerationLogService moderationLog;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;

    public PageResponse<CommentResponse> list(String eventId, Integer page, Integer size) {
        Event e = eventService.requireViewable(eventId);
        boolean admin = currentUser.isAdmin();
        var items = comments.findByEventId(eventId).stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(c -> mapper.toComment(c, e, admin))
                .toList();
        return PageResponse.of(items, page, size);
    }

    public CommentResponse create(String eventId, CreateCommentRequest r) {
        User me = currentUser.require();
        Event e = eventService.require(eventId);
        if (e.getStatus() != EventStatus.PUBLISHED) {
            throw EventService.unavailable(e);
        }
        Comment parent = null;
        if (r.parentId() != null && !r.parentId().isBlank()) {
            parent = comments.findById(r.parentId())
                    .filter(c -> eventId.equals(c.getEventId()))
                    .orElseThrow(() -> ApiException.validation("parent_id", "Комментарий не найден в этом обсуждении"));
        }
        if (r.asOrganizationId() != null && !r.asOrganizationId().isBlank()) {
            organizationService.requireApprovedOwned(r.asOrganizationId(), me.getId());
        }
        Comment c = new Comment();
        c.setId(UUID.randomUUID().toString());
        c.setEventId(eventId);
        c.setAuthorId(me.getId());
        c.setAsOrganizationId(UserService.blankToNull(r.asOrganizationId()));
        c.setParentId(parent == null ? null : parent.getId());
        c.setText(r.text().trim());
        c.setCreatedAt(Instant.now());
        comments.save(c);
        if (parent != null && !parent.getAuthorId().equals(me.getId())) {
            notificationService.notify(parent.getAuthorId(), NotificationType.COMMENT_REPLY,
                    Map.of("event_id", eventId, "event_title", e.getTitle(), "comment_id", c.getId(),
                            "author_name", me.getName()));
        }
        return mapper.toComment(c, e, false);
    }

    public CommentResponse update(String commentId, String text) {
        Comment c = requireOwn(commentId);
        if (c.isHidden()) {
            throw ApiException.conflict("COMMENT_HIDDEN", "Комментарий скрыт модератором");
        }
        c.setText(text.trim());
        c.setUpdatedAt(Instant.now());
        comments.save(c);
        return mapper.toComment(c, eventService.require(c.getEventId()), false);
    }

    public void delete(String commentId) {
        comments.delete(requireOwn(commentId));
    }

    public CommentResponse hide(String commentId, String reason) {
        User admin = currentUser.require();
        Comment c = require(commentId);
        c.setHidden(true);
        c.setHiddenReason(reason);
        comments.save(c);
        moderationLog.log(admin.getId(), ModerationTargetType.COMMENT, commentId, ModerationAction.HIDE, reason);
        return mapper.toComment(c, eventService.require(c.getEventId()), true);
    }

    private Comment require(String id) {
        return comments.findById(id).orElseThrow(() -> ApiException.notFound("Комментарий не найден"));
    }

    private Comment requireOwn(String id) {
        String me = currentUser.require().getId();
        Comment c = require(id);
        if (!me.equals(c.getAuthorId())) {
            throw ApiException.forbidden("Это не ваш комментарий");
        }
        return c;
    }
}
