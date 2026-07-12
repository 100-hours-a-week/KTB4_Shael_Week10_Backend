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
    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 26, message = "제목은 최대 26자까지 가능합니다.")
    private String title;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    @NotEmpty(message = "이미지를 업로드해주세요.")
    @Size(max = 5, message = "이미지는 최대 5장까지 업로드할 수 있습니다.")
    private List<@Size(max = 500, message = "파일 이름은 최대 500자까지 가능합니다.") String> postImage;
}
