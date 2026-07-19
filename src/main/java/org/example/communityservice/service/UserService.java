package org.example.communityservice.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.communityservice.common.Exception.BadRequestException;
import org.example.communityservice.common.Exception.FileStorageException;
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
import org.example.communityservice.storage.ProfileImageStorage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ProfileImageStorage profileImageStorage;
    private static final String DEFAULT_PROFILE_IMAGE = "test_image.png";

    public UserLoginResponseDto login(@Valid UserLoginRequestDto userLoginRequestDto){
        User user = userRepository.findByEmail(userLoginRequestDto.getEmail()).orElseThrow(() -> new UnauthorizedException("login_failed"));

        if(!user.getPassword().equals(userLoginRequestDto.getPassword())){
            throw new UnauthorizedException("login_failed");
        }

        String profileImage = "/images/profiles/" + user.getProfileStoredFilename();
        return new UserLoginResponseDto(user.getUserId(), user.getEmail(), user.getNickname(), profileImage);
    }

    @Transactional
    public void createUser(@Valid UserCreateRequestDto userCreateRequestDto, MultipartFile profileImage){
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

        String profileStoredFilename = null;

        if(profileImage != null && !profileImage.isEmpty()){
            profileStoredFilename = profileImageStorage.store(profileImage);
        }

        try{
            userRepository.save(new User(userCreateRequestDto, profileStoredFilename));
            userRepository.flush();
        }
        catch (RuntimeException e){
            if(profileStoredFilename != null){
                try{
                    profileImageStorage.delete(profileStoredFilename);
                }
                catch (FileStorageException ignored){
                }
            }
            throw e;
        }
    }

    public UserInfoResponseDto showInfo(Long userId){
        return new UserInfoResponseDto(userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("login_required")));
    }

    @Transactional
    public UserInfoResponseDto updateInfo(Long userId, @Valid UserInfoUpdateRequestDto userInfoUpdateRequestDto, MultipartFile profileImage){
        List<ErrorInfoDto> errorInfoDtoList = new ArrayList<>();

        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("login_required"));
        if(userInfoUpdateRequestDto.getEmail()==null && userInfoUpdateRequestDto.getNickname()==null && profileImage==null) {
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

        String oldProfileStoredFilename = user.getProfileStoredFilename();
        String newProfileStoredFilename = null;

        try {
            if (userInfoUpdateRequestDto.getEmail() != null) {
                user.changeEmail(userInfoUpdateRequestDto.getEmail());
            }
            if (userInfoUpdateRequestDto.getNickname() != null) {
                user.changeNickname(userInfoUpdateRequestDto.getNickname());
            }
            if (profileImage != null && !profileImage.isEmpty()) {
                newProfileStoredFilename = profileImageStorage.store(profileImage);
                user.changeProfileStoredFilename(newProfileStoredFilename);
            }

            user.changeUpdatedAt(LocalDateTime.now());
            userRepository.flush();
        }
        catch (RuntimeException e){
            if(newProfileStoredFilename != null){
                try{
                    profileImageStorage.delete(newProfileStoredFilename);
                }
                catch (FileStorageException ignored){
                }
            }
            throw e;
        }
        if(profileImage != null && !profileImage.isEmpty() && !DEFAULT_PROFILE_IMAGE.equals(oldProfileStoredFilename)){
            profileImageStorage.delete(oldProfileStoredFilename);
        }
        return new UserInfoResponseDto(user);
    }

    @Transactional
    public void updatePassword(Long userId, @Valid UserPasswordUpdateRequestDto userPasswordUpdateRequestDto){
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("login_required"));

        user.changePassword(userPasswordUpdateRequestDto.getPassword());
        user.changeUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void withdrawal(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("login_required"));

        String profileStoredFilename = user.getProfileStoredFilename();

        userRepository.delete(user);
        userRepository.flush();

        if(!DEFAULT_PROFILE_IMAGE.equals(profileStoredFilename)){
            profileImageStorage.delete(profileStoredFilename);
        }
    }
}
