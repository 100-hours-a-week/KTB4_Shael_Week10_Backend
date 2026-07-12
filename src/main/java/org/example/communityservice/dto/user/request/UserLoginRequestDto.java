package org.example.communityservice.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserLoginRequestDto {
    @Email(message = "올바른 이메일 주소 형식을 입력해주세요. (예: example@example.com)")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Size(max = 100, message = "이메일은 최대 100자까지 작성 가능합니다.")
    private String email;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(max = 255, message = "비밀번호는 최대 255자까지 작성 가능합니다.")
    private String password;
}
