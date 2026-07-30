package org.example.communityservice.dto.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResultDto {
    private TokenInfoDto tokenInfoDto;
    private String newRefreshToken;
}
