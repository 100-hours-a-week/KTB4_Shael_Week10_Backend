package org.example.communityservice.dto.post.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.example.communityservice.entity.Post;

import java.time.LocalDateTime;

@Getter
@JsonPropertyOrder({
        "postId",
        "title",
        "writerNickname",
        "likeCount",
        "commentCount",
        "viewCount",
        "createdAt"
})
public class PostListResponseDto {
    private Long postId;
    private String title;
    private String writerNickname;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private int likeCount;
    private int commentCount;
    private int viewCount;


    public PostListResponseDto(Post post){
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.writerNickname = post.getUser().getNickname();
        this.likeCount = post.getLikeCount();
        this.commentCount = post.getCommentCount();
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt();
    }
}
