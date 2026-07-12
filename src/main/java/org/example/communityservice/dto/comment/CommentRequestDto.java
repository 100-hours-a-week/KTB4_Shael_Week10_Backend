package org.example.communityservice.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CommentRequestDto {
    private Long parentCommentId;

    @NotBlank(message = "댓글을 입력해주세요.")
    private String content;
}
