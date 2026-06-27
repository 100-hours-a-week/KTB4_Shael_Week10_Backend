package org.example.communityservice.dto.post.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PostUpdateRequestDto {
    @Size(max = 26, message = "title_too_long")
    private String title;

    private String content;

    @Size(max = 5, message = "Maximum allowed images is 5")
    private List<@Size(max = 500, message = "post_image_too_long") String> postImage;
}
