package org.example.communityservice.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginResultDto {
    private UserLoginResponseDto userLoginResponseDto;
    private String refreshToken;
}
