package org.example.communityservice.dto.post.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.example.communityservice.entity.PostImage;

@Getter
@JsonPropertyOrder({
        "postImageId",
        "imageUrl",
        "imageOrder"
})
public class PostImageResponseDto {
    private Long postImageId;
    private String imageUrl;
    private int imageOrder;


    public PostImageResponseDto(PostImage postImage){
        this.postImageId = postImage.getPostImageId();
        this.imageUrl = "/images/posts/" + postImage.getStoredFilename();
        this.imageOrder = postImage.getImageOrder();
    }
}
