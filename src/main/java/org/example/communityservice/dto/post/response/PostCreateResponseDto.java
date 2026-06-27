package org.example.communityservice.dto.post.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.example.communityservice.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonPropertyOrder({
        "postId",
        "title",
        "writerId",
        "writerNickname",
        "writerProfileImage",
        "postImage",
        "content"
})
public class PostCreateResponseDto {
    private Long postId;
    private String title;
    private Long writerId;
    private String writerNickname;
    private String writerProfileImage;
    private List<PostImageResponseDto> postImage;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;


    public PostCreateResponseDto(Post post, List<PostImageResponseDto> postImageResponseDto) {
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.writerId = post.getUser().getUserId();
        this.writerNickname = post.getUser().getNickname();
        this.writerProfileImage = post.getUser().getProfileImage();
        this.postImage = postImageResponseDto;
        this.content = post.getContent();
        this.createdAt = post.getCreatedAt();
    }
}
