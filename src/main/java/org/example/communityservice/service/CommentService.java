package org.example.communityservice.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.communityservice.common.exception.*;
import org.example.communityservice.common.dto.ErrorInfoDto;
import org.example.communityservice.common.dto.ErrorResponseDto;
import org.example.communityservice.dto.comment.CommentRequestDto;
import org.example.communityservice.dto.comment.response.CommentCreateResponseDto;
import org.example.communityservice.dto.comment.response.CommentDeleteResponseDto;
import org.example.communityservice.dto.comment.response.CommentUpdateResponseDto;
import org.example.communityservice.entity.Comment;
import org.example.communityservice.entity.Post;
import org.example.communityservice.entity.User;
import org.example.communityservice.repository.CommentRepository;
import org.example.communityservice.repository.PostRepository;
import org.example.communityservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;


    @Transactional
    public CommentCreateResponseDto createComment(Long userId, Long postId, @Valid CommentRequestDto commentRequestDto) {
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post", "not_exist")))));

        Comment parentComment;
        if(commentRequestDto.getParentCommentId()!=null){
            parentComment = commentRepository.findById(commentRequestDto.getParentCommentId()).orElseThrow(() -> new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("parent_comment", "not_exist")))));
        }
        else{
            parentComment = null;
        }
        Comment comment = new Comment(post, parentComment, user, commentRequestDto.getContent());
        commentRepository.save(comment);
        post.increaseCommentCount();
        int commentCount = post.getCommentCount();

        return new CommentCreateResponseDto(comment, user, commentCount);
    }

    @Transactional
    public CommentUpdateResponseDto updateComment(Long userId, Long postId, Long commentId, @Valid CommentRequestDto commentRequestDto) {
        userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required"));
        postRepository.findById(postId).orElseThrow(() -> new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post", "not_exist")))));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("comment", "not_exist")))));
        if(!userId.equals(comment.getUser().getUserId())){
            throw new ForbiddenException();
        }

        comment.changeContent(commentRequestDto.getContent());
        comment.changeUpdatedAt();
        return new CommentUpdateResponseDto(commentRequestDto.getContent(), comment.getUpdatedAt());
    }

    @Transactional
    public CommentDeleteResponseDto deleteComment(Long userId, Long postId, Long commentId) {
        userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("post", "not_exist")))));
        Comment comment = commentRepository.findByCommentIdAndDeletedAtIsNull(commentId).orElseThrow(() -> new NotFoundException("not_found", new ErrorResponseDto(List.of(new ErrorInfoDto("comment", "not_exist")))));
        if(!userId.equals(comment.getUser().getUserId())){
            throw new ForbiddenException();
        }
        if(comment.getDeletedAt()!=null){
            throw new BadRequestException("invalid_request");
        }
        comment.deleteComment();
        post.decreaseCommentCount();
        int commentCount = post.getCommentCount();

        return new CommentDeleteResponseDto(commentCount);
    }
}
