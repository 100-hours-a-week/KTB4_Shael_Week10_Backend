package org.example.communityservice.dto.post.response;

import lombok.Getter;

@Getter
public class PostLikeCountResponseDto {
    private int likeCount;

    public PostLikeCountResponseDto(int likeCount){
        this.likeCount = likeCount;
    }
}
