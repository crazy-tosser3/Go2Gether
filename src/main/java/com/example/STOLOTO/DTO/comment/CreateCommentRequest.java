package com.example.STOLOTO.DTO.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank @Size(max = 2000) String text,
        Long parentId,
        Long asOrganizationId) {
}
