package org.example.communityservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.communityservice.common.dto.CommonResponseDto;
import org.example.communityservice.dto.user.request.UserCreateRequestDto;
import org.example.communityservice.dto.user.request.UserInfoUpdateRequestDto;
import org.example.communityservice.dto.user.request.UserLoginRequestDto;
import org.example.communityservice.dto.user.request.UserPasswordUpdateRequestDto;
import org.example.communityservice.dto.user.response.UserInfoResponseDto;
import org.example.communityservice.dto.user.response.UserLoginResponseDto;
import org.example.communityservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<CommonResponseDto<UserLoginResponseDto>> login(@Valid @RequestBody UserLoginRequestDto userLoginRequestDto) {
        UserLoginResponseDto userLoginResponseDto = userService.login(userLoginRequestDto);

        return ResponseEntity.ok(new CommonResponseDto<>("login_success", userLoginResponseDto));
    }

    @PostMapping("/signup")
    public ResponseEntity<CommonResponseDto<Void>> createUser(@Valid @RequestBody UserCreateRequestDto UserCreateRequestDto){
        userService.createUser(UserCreateRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new CommonResponseDto<>("register_success", null));
    }

    @GetMapping("/user/{userId}/info")
    public ResponseEntity<CommonResponseDto<UserInfoResponseDto>> showInfo(@PathVariable Long userId){
        UserInfoResponseDto userInfoResponseDto = userService.showInfo(userId);

        return ResponseEntity.ok(new CommonResponseDto<>("fetch_success", userInfoResponseDto));
    }

    @PatchMapping("/user/{userId}/info")
    public ResponseEntity<CommonResponseDto<UserInfoResponseDto>> updateInfo(@PathVariable Long userId, @Valid @RequestBody UserInfoUpdateRequestDto userInfoUpdateRequestDto){
        UserInfoResponseDto userInfoResponseDto = userService.updateInfo(userId, userInfoUpdateRequestDto);

        return ResponseEntity.ok(new CommonResponseDto<>("update_success", userInfoResponseDto));
    }

    @PatchMapping("/user/{userId}/password")
    public ResponseEntity<CommonResponseDto<Void>> updatePassword(@PathVariable Long userId, @Valid @RequestBody UserPasswordUpdateRequestDto userPasswordUpdateRequestDto){
        userService.updatePassword(userId, userPasswordUpdateRequestDto);

        return ResponseEntity.ok(new CommonResponseDto<>("update_success", null));
    }

    @DeleteMapping("/{userId}/withdrawal")
    public ResponseEntity<CommonResponseDto<Void>> withdrawal(@PathVariable Long userId){
        userService.withdrawal(userId);

        return ResponseEntity.ok(new CommonResponseDto<>("delete_success", null));
    }
}
