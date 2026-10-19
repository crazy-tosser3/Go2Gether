package com.example.STOLOTO.repository;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import com.example.STOLOTO.Models.Comment;

public interface CommentRepository extends ListCrudRepository<Comment, String> {
    List<Comment> findByEventId(String eventId);
}
