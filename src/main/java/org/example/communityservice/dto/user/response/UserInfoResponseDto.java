package org.example.communityservice.dto.user.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.example.communityservice.entity.User;

@Getter
@JsonPropertyOrder({
        "email",
        "nickname",
        "profileImage"
})
public class UserInfoResponseDto {
    private String email;
    private String nickname;
    private String profileImage;

    public UserInfoResponseDto(User user){
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.profileImage = user.getProfileImage();
    }
}
