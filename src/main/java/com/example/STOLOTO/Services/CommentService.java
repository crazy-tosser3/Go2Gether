package com.example.STOLOTO.Services;

import java.time.Instant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.STOLOTO.DTO.comment.CommentResponse;
import com.example.STOLOTO.DTO.comment.CreateCommentRequest;
import com.example.STOLOTO.DTO.common.PageResponse;
import com.example.STOLOTO.Models.AuditAction;
import com.example.STOLOTO.Models.Comment;
import com.example.STOLOTO.Models.Event;
import com.example.STOLOTO.Models.EventStatus;
import com.example.STOLOTO.Models.NotificationType;
import com.example.STOLOTO.Models.User;
import com.example.STOLOTO.cache.CacheRegions;
import com.example.STOLOTO.cache.PageCache;
import com.example.STOLOTO.exception.ApiException;
import com.example.STOLOTO.repository.CommentRepository;
import com.example.STOLOTO.security.CurrentUser;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.type.TypeReference;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository comments;
    private final EventService eventService;
    private final OrganizationService organizationService;
    private final NotificationService notificationService;
    private final AuditService audit;
    private final CurrentUser currentUser;
    private final DtoMapper mapper;
    private final PageCache cache;
    private final CacheRegions regions;

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> list(Long eventId, Integer page, Integer size) {
        eventService.requireViewableCard(eventId);
        var pageable = PageResponse.pageable(page, size, Sort.unsorted());
        if (currentUser.isAdmin()) {
            return load(eventId, pageable, true); // админ видит причины скрытия — мимо кэша
        }
        return cache.get(CacheRegions.comments(eventId), "p" + pageable.getPageNumber() + ":" + pageable.getPageSize(),
                CacheRegions.COMMENTS_TTL, new TypeReference<PageResponse<CommentResponse>>() {},
                () -> load(eventId, pageable, false));
    }

    private PageResponse<CommentResponse> load(Long eventId, Pageable pageable, boolean admin) {
        Event e = eventService.require(eventId);
        return PageResponse.of(comments.findByEventIdOrderByCreatedAtAsc(eventId, pageable),
                c -> mapper.toComment(c, e, admin));
    }

    public CommentResponse create(Long eventId, CreateCommentRequest r) {
        User me = currentUser.require();
        Event e = eventService.require(eventId);
        if (e.getStatus() != EventStatus.PUBLISHED) {
            throw EventService.unavailable(e);
        }
        Comment parent = null;
        if (r.parentId() != null) {
            parent = comments.findById(r.parentId())
                    .filter(c -> eventId.equals(c.getEventId()))
                    .orElseThrow(() -> ApiException.validation("parent_id", "Комментарий не найден в этом обсуждении"));
        }
        if (r.asOrganizationId() != null) {
            organizationService.requireApprovedOwned(r.asOrganizationId(), me.getId());
        }
        Comment c = new Comment();
        c.setEventId(eventId);
        c.setAuthorId(me.getId());
        c.setAsOrganizationId(r.asOrganizationId());
        // Один уровень вложенности: ответ на ответ привязываем к корневому комментарию
        c.setParentId(parent == null ? null : parent.getParentId() != null ? parent.getParentId() : parent.getId());
        c.setText(r.text().trim());
        c.setCreatedAt(Instant.now());
        comments.save(c);
        regions.commentsChanged(eventId);
        if (parent != null && !parent.getAuthorId().equals(me.getId())) {
            notificationService.notify(parent.getAuthorId(), NotificationType.COMMENT_REPLY, "Новый ответ",
                    me.getName() + " ответил(а) в обсуждении «" + e.getTitle() + "»", eventId);
        }
        // Новый вопрос к событию — организатору (если пишет не он сам)
        if (parent == null && !mapper.isOrganizer(e, me.getId())) {
            notificationService.notify(mapper.organizerRecipient(e), NotificationType.NEW_COMMENT,
                    "Новый вопрос в обсуждении",
                    me.getName() + " написал(а) в обсуждении «" + e.getTitle() + "»", eventId);
        }
        return mapper.toComment(c, e, false);
    }

    public CommentResponse update(Long commentId, String text) {
        Comment c = requireOwn(commentId);
        if (c.isHidden()) {
            throw ApiException.conflict("COMMENT_HIDDEN", "Комментарий скрыт модератором");
        }
        c.setText(text.trim());
        c.setUpdatedAt(Instant.now());
        regions.commentsChanged(c.getEventId());
        return mapper.toComment(c, eventService.require(c.getEventId()), false);
    }

    public void delete(Long commentId) {
        Comment c = requireOwn(commentId);
        comments.delete(c);
        regions.commentsChanged(c.getEventId());
    }

    public CommentResponse hide(Long commentId, String reason) {
        User admin = currentUser.require();
        Comment c = require(commentId);
        c.setHidden(true);
        c.setHiddenReason(reason);
        c.setHiddenBy(admin.getId());
        c.setHiddenAt(Instant.now());
        regions.commentsChanged(c.getEventId());
        audit.comment(admin.getId(), commentId, AuditAction.COMMENT_HIDDEN, reason);
        return mapper.toComment(c, eventService.require(c.getEventId()), true);
    }

    private Comment require(Long id) {
        return comments.findById(id).orElseThrow(() -> ApiException.notFound("Комментарий не найден"));
    }

    private Comment requireOwn(Long id) {
        Long me = currentUser.require().getId();
        Comment c = require(id);
        if (!me.equals(c.getAuthorId())) {
            throw ApiException.forbidden("Это не ваш комментарий");
        }
        return c;
    }
}
