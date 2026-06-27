package org.example.communityservice.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommentRequestDto {
    private Long parentCommentId;

    @NotBlank(message = "content_required")
    private String content;
}
