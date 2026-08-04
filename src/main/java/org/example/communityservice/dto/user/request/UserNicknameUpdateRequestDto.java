package org.example.communityservice.dto.user.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserNicknameUpdateRequestDto {
    @Size(max = 10, message = "닉네임은 최대 10자까지 작성 가능합니다.")
    private String nickname;
}