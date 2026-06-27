package org.example.communityservice.dto.post.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.example.communityservice.dto.comment.response.CommentResponseDto;
import org.example.communityservice.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonPropertyOrder({
        "title",
        "writerId",
        "writerNickname",
        "writerProfileImage",
        "postImage",
        "content",
        "createdAt",
        "updatedAt",
        "likeCount",
        "commentCount",
        "viewCount",
        "commentList"
})
public class PostDetailResponseDto {
    private String title;
    private Long writerId;
    private String writerNickname;
    private String writerProfileImage;
    private List<PostImageResponseDto> postImage;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private int likeCount;
    private int commentCount;
    private int viewCount;
    private List<CommentResponseDto> commentList;


    public PostDetailResponseDto(Post post, List<PostImageResponseDto> postImageResponseDto, List<CommentResponseDto> commentList){
        this.title = post.getTitle();
        this.writerId = post.getUser().getUserId();
        this.writerNickname = post.getUser().getNickname();
        this.writerProfileImage = post.getUser().getProfileImage();
        this.postImage = postImageResponseDto;
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.viewCount = post.getViewCount();
        this.commentList = commentList;
    }
}
