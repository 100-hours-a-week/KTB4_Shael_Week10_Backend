package org.example.communityservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.communityservice.dto.comment.CommentRequestDto;
import org.example.communityservice.dto.comment.response.CommentCreateResponseDto;
import org.example.communityservice.common.dto.CommonResponseDto;
import org.example.communityservice.dto.comment.response.CommentDeleteResponseDto;
import org.example.communityservice.dto.comment.response.CommentUpdateResponseDto;
import org.example.communityservice.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/{userId}/posts/{postId}/comment")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommonResponseDto<CommentCreateResponseDto>> createComment(@PathVariable Long userId, @PathVariable Long postId, @Valid @RequestBody CommentRequestDto commentRequestDto){
        CommentCreateResponseDto commentCreateResponseDto = commentService.createComment(userId, postId, commentRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CommonResponseDto<>("register_success", commentCreateResponseDto));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommonResponseDto<CommentUpdateResponseDto>> updateComment(@PathVariable Long userId, @PathVariable Long postId, @PathVariable Long commentId, @Valid @RequestBody CommentRequestDto commentRequestDto){
        CommentUpdateResponseDto commentUpdateResponseDto = commentService.updateComment(userId, postId, commentId, commentRequestDto);

        return ResponseEntity.ok(new CommonResponseDto<>("update_success", commentUpdateResponseDto));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommonResponseDto<CommentDeleteResponseDto>> deleteComment(@PathVariable Long userId, @PathVariable Long postId, @PathVariable Long commentId){
        CommentDeleteResponseDto commentDeleteResponseDto = commentService.deleteComment(userId, postId, commentId);

        return ResponseEntity.ok(new CommonResponseDto<>("delete_success", commentDeleteResponseDto));
    }
}
