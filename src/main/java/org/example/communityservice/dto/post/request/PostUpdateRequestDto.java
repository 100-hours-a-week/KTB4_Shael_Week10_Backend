package org.example.communityservice.dto.post.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class PostUpdateRequestDto {
    @Size(max = 26, message = "제목은 최대 26자까지 가능합니다.")
    private String title;

    private String content;

    @Size(max = 5, message = "이미지는 최대 5장까지 업로드할 수 있습니다.")
    private List<@Size(max = 500, message = "파일 이름은 최대 500자까지 가능합니다.") String> postImage;
}
