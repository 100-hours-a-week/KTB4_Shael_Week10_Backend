package org.example.communityservice.dto.comment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.example.communityservice.entity.Comment;
import org.example.communityservice.entity.User;

import java.time.LocalDateTime;

@Getter
@JsonPropertyOrder({
        "commentId",
        "parentCommentId",
        "writerId",
        "writerProfileImage",
        "writerNickname",
        "isOwner",
        "content",
        "createdAt",
        "commentCount"
})
public class CommentCreateResponseDto {
    private Long commentId;
    private Long parentCommentId;
    private Long writerId;
    private String writerProfileImage;
    private String writerNickname;
    private boolean isOwner;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private int commentCount;

    public CommentCreateResponseDto(Comment comment, User user, int commentCount){
        this.commentId = comment.getCommentId();
        if(comment.getParentComment()!=null){
            this.parentCommentId = comment.getParentComment().getCommentId();
        }
        else {
            this.parentCommentId = null;
        }
        this.writerId = user.getUserId();
        this.writerProfileImage = user.getProfileImage();
        this.writerNickname = user.getNickname();
        this.isOwner = (comment.getUser().getUserId()).equals(user.getUserId());
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.commentCount = commentCount;
    }
}
