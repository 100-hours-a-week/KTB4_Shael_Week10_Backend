package org.example.communityservice.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserPasswordUpdateRequestDto {
    @NotBlank(message = "password_required")
    @Size(max = 255, message = "password_too_long")
    private String password;
}
