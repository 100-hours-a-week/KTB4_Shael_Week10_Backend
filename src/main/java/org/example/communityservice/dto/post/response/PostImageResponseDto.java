package org.example.communityservice.dto.post.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.example.communityservice.entity.PostImage;

@Getter
@JsonPropertyOrder({
        "postImageId",
        "postImage",
        "imageOrder"
})
public class PostImageResponseDto {
    private Long postImageId;
    private String postImage;
    private int imageOrder;


    public PostImageResponseDto(PostImage postImage){
        this.postImageId = postImage.getPostImageId();
        this.postImage = postImage.getPostImage();
        this.imageOrder = postImage.getImageOrder();
    }
}
