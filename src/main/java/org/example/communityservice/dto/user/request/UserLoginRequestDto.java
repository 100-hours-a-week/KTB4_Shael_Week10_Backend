package org.example.communityservice.dto.user.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserLoginRequestDto {
    @Email(message = "invalid_email")
    @NotBlank(message = "email_required")
    @Size(max = 100, message = "email_too_long")
    private String email;

    @NotBlank(message = "password_required")
    @Size(max = 255, message = "password_too_long")
    private String password;
}
