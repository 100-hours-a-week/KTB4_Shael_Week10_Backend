package org.example.communityservice.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoUpdateRequestDto {
    @Email(message = "올바른 이메일 주소 형식을 입력해주세요. (예: example@example.com)")
    @Size(max = 100, message = "이메일은 최대 100자까지 작성 가능합니다.")
    private String email;

    @Size(max = 10, message = "닉네임은 최대 10자까지 작성 가능합니다.")
    private String nickname;
}