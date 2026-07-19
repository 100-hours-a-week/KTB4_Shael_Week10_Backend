package org.example.communityservice.dto.post.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PostUpdateRequestDto {
    @Size(max = 26, message = "제목은 최대 26자까지 가능합니다.")
    private String title;

    private String content;
}
