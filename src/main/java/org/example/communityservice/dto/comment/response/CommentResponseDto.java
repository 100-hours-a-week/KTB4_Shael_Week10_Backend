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
        "content",
        "createdAt"
})
public class CommentResponseDto {
    private Long commentId;
    private Long parentCommentId;
    private Long writerId;
    private String writerProfileImage;
    private String writerNickname;
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
        this.writerId = user.getUserId();
        this.writerProfileImage = user.getProfileImage();
        this.writerNickname = user.getNickname();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
    }
}
