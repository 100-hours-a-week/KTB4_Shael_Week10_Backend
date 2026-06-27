package org.example.communityservice.dto.post.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@JsonPropertyOrder({
        "title",
        "postImage",
        "content",
        "updatedAt"
})
public class PostUpdateResponseDto {
    private String title;
    private List<PostImageResponseDto> postImage;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;


    public PostUpdateResponseDto(String title, List<PostImageResponseDto> postImageResponseDto, String content, LocalDateTime updatedAt){
        this.title = title;
        this.postImage = postImageResponseDto;
        this.content = content;
        this.updatedAt = updatedAt;
    }
}
