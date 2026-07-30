package org.example.communityservice.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.communityservice.dto.token.TokenInfoDto;
import org.example.communityservice.entity.User;

@Getter
@AllArgsConstructor
public class UserLoginResponseDto {
    private Long userId;
    private String email;
    private String nickname;
    private String profileImage;
    private TokenInfoDto token;

    public static UserLoginResponseDto of(User user, String accessToken, long expiresIn){
        String profileImage = "/images/profiles/" + user.getProfileStoredFilename();
        return new UserLoginResponseDto(user.getUserId(), user.getEmail(), user.getNickname(), profileImage, new TokenInfoDto(accessToken, expiresIn));
    }
}
