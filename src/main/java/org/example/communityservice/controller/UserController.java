package org.example.communityservice.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.communityservice.common.dto.CommonResponseDto;
import org.example.communityservice.dto.token.TokenInfoDto;
import org.example.communityservice.dto.token.TokenResultDto;
import org.example.communityservice.dto.user.request.UserCreateRequestDto;
import org.example.communityservice.dto.user.request.UserNicknameUpdateRequestDto;
import org.example.communityservice.dto.user.request.UserLoginRequestDto;
import org.example.communityservice.dto.user.request.UserPasswordUpdateRequestDto;
import org.example.communityservice.dto.user.response.UserInfoResponseDto;
import org.example.communityservice.dto.user.response.UserLoginResponseDto;
import org.example.communityservice.dto.user.response.UserLoginResultDto;
import org.example.communityservice.service.UserService;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<CommonResponseDto<UserLoginResponseDto>> login(
            @Valid @RequestBody UserLoginRequestDto userLoginRequestDto, HttpServletResponse httpServletResponse) {
        UserLoginResultDto userLoginResultDto = userService.login(userLoginRequestDto);

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", userLoginResultDto.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/token")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok(new CommonResponseDto<>("login_success", userLoginResultDto.getUserLoginResponseDto()));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<CommonResponseDto<TokenInfoDto>> refreshAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse httpServletResponse){
        TokenResultDto tokenResultDto = userService.refreshAccessToken(refreshToken);

        if(tokenResultDto.getNewRefreshToken() != null){
            ResponseCookie responseCookie = ResponseCookie.from("refreshToken", tokenResultDto.getNewRefreshToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/token")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();
            httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
        }

        return ResponseEntity.ok(new CommonResponseDto<>("TOKEN_REFRESH_SUCCESS", tokenResultDto.getTokenInfoDto()));
    }

    @PostMapping("/logout")
    public ResponseEntity<CommonResponseDto<Void>> logout(
            @AuthenticationPrincipal Long userId,
            HttpServletResponse httpServletResponse) {
        userService.logout(userId);

        ResponseCookie expiredRefreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/token")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, expiredRefreshCookie.toString());

        return ResponseEntity.ok(new CommonResponseDto<>("logout_success", null));
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponseDto<Void>> createUser(
            @Valid @RequestPart("content") UserCreateRequestDto userCreateRequestDto,
            @RequestPart(value = "profileImage", required = false)MultipartFile profileImage){
        userService.createUser(userCreateRequestDto, profileImage);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CommonResponseDto<>("register_success", null));
    }

    @GetMapping("/user/info")
    public ResponseEntity<CommonResponseDto<UserInfoResponseDto>> showInfo(
            @AuthenticationPrincipal Long userId){
        UserInfoResponseDto userInfoResponseDto = userService.showInfo(userId);

        return ResponseEntity.ok(new CommonResponseDto<>("fetch_success", userInfoResponseDto));
    }

    @PatchMapping(value = "/user/info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponseDto<UserInfoResponseDto>> updateInfo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestPart("content") UserNicknameUpdateRequestDto userNicknameUpdateRequestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage){
        UserInfoResponseDto userInfoResponseDto = userService.updateInfo(userId, userNicknameUpdateRequestDto, profileImage);

        return ResponseEntity.ok(new CommonResponseDto<>("update_success", userInfoResponseDto));
    }

    @PatchMapping("/user/password")
    public ResponseEntity<CommonResponseDto<Void>> updatePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserPasswordUpdateRequestDto userPasswordUpdateRequestDto){
        userService.updatePassword(userId, userPasswordUpdateRequestDto);

        return ResponseEntity.ok(new CommonResponseDto<>("update_success", null));
    }

    @DeleteMapping("/withdrawal")
    public ResponseEntity<CommonResponseDto<Void>> withdrawal(
            @AuthenticationPrincipal Long userId){
        userService.withdrawal(userId);

        return ResponseEntity.ok(new CommonResponseDto<>("delete_success", null));
    }
}
