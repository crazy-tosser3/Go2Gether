package com.example.STOLOTO.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.STOLOTO.Models.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByEventIdOrderByCreatedAtAsc(Long eventId, Pageable pageable);

    List<Comment> findByEventIdOrderByCreatedAtAsc(Long eventId);

    long countByHiddenTrue();
}
