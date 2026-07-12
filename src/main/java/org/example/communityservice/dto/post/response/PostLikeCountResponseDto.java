package org.example.communityservice.dto.post.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
        "isLiked",
        "likeCount"
})
public class PostLikeCountResponseDto {
    private boolean isLiked;
    private int likeCount;

    public PostLikeCountResponseDto(boolean isLiked, int likeCount){
        this.isLiked = isLiked;
        this.likeCount = likeCount;
    }
}
