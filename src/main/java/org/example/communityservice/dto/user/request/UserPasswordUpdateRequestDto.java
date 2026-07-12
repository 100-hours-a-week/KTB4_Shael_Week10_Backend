package org.example.communityservice.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserPasswordUpdateRequestDto {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(max = 255, message = "비밀번호는 최대 255자까지 작성 가능합니다.")
    private String password;
}
