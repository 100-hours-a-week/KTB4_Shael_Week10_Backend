package org.example.communityservice.dto.post.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PostRequestDto {
    @NotBlank(message = "title_required")
    @Size(max = 26, message = "title_too_long")
    private String title;

    @NotBlank(message = "content_required")
    private String content;

    @NotEmpty(message = "post_image_required")
    @Size(max = 5, message = "Maximum allowed images is 5")
    private List<@Size(max = 500, message = "post_image_too_long") String> postImage;
}
