package org.example.communityservice.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.communityservice.common.Exception.BadRequestException;
import org.example.communityservice.common.Exception.UnauthorizedException;
import org.example.communityservice.common.dto.ErrorInfoDto;
import org.example.communityservice.common.dto.ErrorResponseDto;
import org.example.communityservice.dto.user.request.UserCreateRequestDto;
import org.example.communityservice.dto.user.request.UserInfoUpdateRequestDto;
import org.example.communityservice.dto.user.request.UserLoginRequestDto;
import org.example.communityservice.dto.user.request.UserPasswordUpdateRequestDto;
import org.example.communityservice.dto.user.response.UserInfoResponseDto;
import org.example.communityservice.dto.user.response.UserLoginResponseDto;
import org.example.communityservice.entity.User;
import org.example.communityservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserLoginResponseDto login(@Valid UserLoginRequestDto userLoginRequestDto){
        User user = userRepository.findByEmail(userLoginRequestDto.getEmail()).orElseThrow(() -> new UnauthorizedException("not_exist"));

        if(!user.getPassword().equals(userLoginRequestDto.getPassword())){
            throw new UnauthorizedException("login_failed");
        }
        return new UserLoginResponseDto(user.getUserId(), user.getEmail(), user.getNickname(), user.getProfileImage());
    }

    public void createUser(@Valid UserCreateRequestDto userCreateRequestDto){
        List<ErrorInfoDto> errorInfoDtoList = new ArrayList<>();

        if(userRepository.existsByEmail(userCreateRequestDto.getEmail())) {
            errorInfoDtoList.add(new ErrorInfoDto("email", "중복된 이메일입니다."));
        }
        if(userRepository.existsByNickname(userCreateRequestDto.getNickname())){
            errorInfoDtoList.add(new ErrorInfoDto("nickname", "중복된 닉네임입니다."));
        }
        if(!errorInfoDtoList.isEmpty()){
            throw new BadRequestException("invalid_request", new ErrorResponseDto(errorInfoDtoList));
        }

        userRepository.save(new User(userCreateRequestDto));
    }

    public UserInfoResponseDto showInfo(Long userId){
        return new UserInfoResponseDto(userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("not_exist")));
    }

    @Transactional
    public UserInfoResponseDto updateInfo(Long userId, @Valid UserInfoUpdateRequestDto userInfoUpdateRequestDto){
        List<ErrorInfoDto> errorInfoDtoList = new ArrayList<>();

        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("not_exist"));
        if(userInfoUpdateRequestDto.getEmail()==null && userInfoUpdateRequestDto.getNickname()==null && userInfoUpdateRequestDto.getProfileImage()==null) {
            throw new BadRequestException("invalid_request");
        }
        if(userInfoUpdateRequestDto.getEmail()!=null && !user.getEmail().equals(userInfoUpdateRequestDto.getEmail()) && userRepository.existsByEmail(userInfoUpdateRequestDto.getEmail())) {
            errorInfoDtoList.add(new ErrorInfoDto("email", "중복된 이메일입니다."));
        }
        if(userInfoUpdateRequestDto.getNickname()!=null && !user.getNickname().equals(userInfoUpdateRequestDto.getNickname()) && userRepository.existsByNickname(userInfoUpdateRequestDto.getNickname())){
            errorInfoDtoList.add(new ErrorInfoDto("nickname", "중복된 닉네임입니다."));
        }
        if(!errorInfoDtoList.isEmpty()){
            throw new BadRequestException("invalid_request", new ErrorResponseDto(errorInfoDtoList));
        }

        if(userInfoUpdateRequestDto.getEmail()!=null){
            user.changeEmail(userInfoUpdateRequestDto.getEmail());
        }
        if(userInfoUpdateRequestDto.getNickname()!=null){
            user.changeNickname(userInfoUpdateRequestDto.getNickname());
        }
        if(userInfoUpdateRequestDto.getProfileImage()!=null){
            user.changeProfileImage(userInfoUpdateRequestDto.getProfileImage());
        }

        user.changeUpdatedAt(LocalDateTime.now());
        return new UserInfoResponseDto(user);
    }

    @Transactional
    public void updatePassword(Long userId, @Valid UserPasswordUpdateRequestDto userPasswordUpdateRequestDto){
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("not_exist"));

        user.changePassword(userPasswordUpdateRequestDto.getPassword());
        user.changeUpdatedAt(LocalDateTime.now());
    }

    public void withdrawal(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("not_exist"));
        userRepository.delete(user);
    }
}
