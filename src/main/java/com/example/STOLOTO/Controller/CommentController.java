package com.example.STOLOTO.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.STOLOTO.DTO.comment.CommentResponse;
import com.example.STOLOTO.DTO.comment.UpdateCommentRequest;
import com.example.STOLOTO.Services.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PatchMapping("/{cid}")
    public CommentResponse update(@PathVariable Long cid, @Valid @RequestBody UpdateCommentRequest request) {
        return commentService.update(cid, request.text());
    }

    @DeleteMapping("/{cid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long cid) {
        commentService.delete(cid);
    }
}
