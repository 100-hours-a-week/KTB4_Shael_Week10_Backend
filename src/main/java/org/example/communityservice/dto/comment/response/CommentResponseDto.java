package org.example.communityservice.dto.comment.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.example.communityservice.common.util.UserDisplayUtils;
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
public class CommentResponseDto {
    private Long commentId;
    private Long parentCommentId;
    private Long writerId;
    private String writerProfileImage;
    private String writerNickname;
    private boolean isOwner;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;


    public CommentResponseDto(Comment comment, User user){
        this.commentId = comment.getCommentId();
        if(comment.getParentComment()!=null){
            this.parentCommentId = comment.getParentComment().getCommentId();
        }
        else {
            this.parentCommentId = null;
        }
        this.writerId = comment.getUser().getUserId();
        this.writerProfileImage = UserDisplayUtils.profileStoredFilename(comment.getUser());
        this.writerNickname = UserDisplayUtils.nickname(comment.getUser());
        this.isOwner = (comment.getUser().getUserId()).equals(user.getUserId());
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
    }
}
