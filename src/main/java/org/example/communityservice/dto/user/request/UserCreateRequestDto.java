package org.example.communityservice.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequestDto {

    @Email(message = "invalid_email")
    @NotBlank(message = "email_required")
    @Size(max = 100, message = "email_too_long")
    private String email;

    @NotBlank(message = "password_required")
    @Size(max = 255, message = "password_too_long")
    private String password;

    @NotBlank(message = "nickname_required")
    @Size(max = 10, message = "nickname_too_long")
    private String nickname;

    @Size(max = 500, message = "profile_image_too_long")
    private String profileImage;
}
