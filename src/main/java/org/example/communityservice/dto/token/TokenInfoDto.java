package org.example.communityservice.dto.token;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenInfoDto {
    private String accessToken;
    private long expiresIn;
}
