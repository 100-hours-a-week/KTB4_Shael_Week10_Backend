package org.example.communityservice.service;

import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.communityservice.common.exception.BadRequestException;
import org.example.communityservice.common.exception.FileStorageException;
import org.example.communityservice.common.exception.UnauthorizedException;
import org.example.communityservice.common.dto.ErrorInfoDto;
import org.example.communityservice.common.dto.ErrorResponseDto;
import org.example.communityservice.common.security.JwtProvider;
import org.example.communityservice.common.security.RefreshTokenHasher;
import org.example.communityservice.dto.token.TokenInfoDto;
import org.example.communityservice.dto.token.TokenResultDto;
import org.example.communityservice.dto.user.request.UserCreateRequestDto;
import org.example.communityservice.dto.user.request.UserNicknameUpdateRequestDto;
import org.example.communityservice.dto.user.request.UserLoginRequestDto;
import org.example.communityservice.dto.user.request.UserPasswordUpdateRequestDto;
import org.example.communityservice.dto.user.response.UserInfoResponseDto;
import org.example.communityservice.dto.user.response.UserLoginResponseDto;
import org.example.communityservice.dto.user.response.UserLoginResultDto;
import org.example.communityservice.entity.RefreshToken;
import org.example.communityservice.entity.User;
import org.example.communityservice.repository.RefreshTokenRepository;
import org.example.communityservice.repository.UserRepository;
import org.example.communityservice.storage.ProfileImageStorage;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenRevoker refreshTokenRevoker;
    private final JwtProvider jwtProvider;
    private final RefreshTokenHasher refreshTokenHasher;
    private final PasswordEncoder passwordEncoder;
    private final ProfileImageStorage profileImageStorage;
    private static final String DEFAULT_PROFILE_IMAGE = "test_image.png";

    @Transactional
    public UserLoginResultDto login(@Valid UserLoginRequestDto userLoginRequestDto){
        User user = userRepository.findByEmailAndDeletedAtIsNull(userLoginRequestDto.getEmail()).orElseThrow(() -> new UnauthorizedException("login_failed"));

        if(!passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword())){
            throw new UnauthorizedException("login_failed");
        }

        String accessToken = jwtProvider.createAccessToken(user.getUserId());

        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());
        String refreshTokenHash = refreshTokenHasher.hash(refreshToken);
        refreshTokenRepository.deleteByUserId(user.getUserId());
        refreshTokenRepository.save(new RefreshToken(
                refreshTokenHash,
                user.getUserId(),
                LocalDateTime.now().plusSeconds(jwtProvider.getRefreshTokenValidityInSeconds())));

        return new UserLoginResultDto(UserLoginResponseDto.of(user, accessToken, jwtProvider.getAccessTokenValidityInMilliseconds()), refreshToken);
    }

    @Transactional
    public TokenResultDto refreshAccessToken(String refreshToken){
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("unauthorized");
        }

        String refreshTokenHash = refreshTokenHasher.hash(refreshToken);
        RefreshToken saved = refreshTokenRepository.findByToken(refreshTokenHash).orElseThrow(() -> new UnauthorizedException("unauthorized"));

        if(saved.isExpired()){
            refreshTokenRevoker.revokeHash(refreshTokenHash);
            throw new UnauthorizedException("unauthorized");
        }

        Long tokenUserId;
        try {
            tokenUserId = jwtProvider.getRefreshTokenUserId(refreshToken);
        } catch (JwtException | IllegalArgumentException exception) {
            refreshTokenRevoker.revokeHash(refreshTokenHash);
            throw new UnauthorizedException("unauthorized");
        }

        if (!tokenUserId.equals(saved.getUserId())) {
            refreshTokenRevoker.revokeHash(refreshTokenHash);
            throw new UnauthorizedException("unauthorized");
        }

        User user = userRepository.findByUserIdAndDeletedAtIsNull(saved.getUserId()).orElseThrow(() -> new UnauthorizedException("unauthorized"));

        String newAccessToken = jwtProvider.createAccessToken(user.getUserId());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getUserId());
        String newRefreshTokenHash = refreshTokenHasher.hash(newRefreshToken);
        refreshTokenRepository.delete(saved);
        refreshTokenRepository.save(new RefreshToken(
                newRefreshTokenHash,
                user.getUserId(),
                LocalDateTime.now().plusSeconds(jwtProvider.getRefreshTokenValidityInSeconds())));

        return new TokenResultDto(
                new TokenInfoDto(newAccessToken, jwtProvider.getAccessTokenValidityInMilliseconds()),
                newRefreshToken);
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
            String encodedPassword = passwordEncoder.encode(userCreateRequestDto.getPassword());
            userRepository.save(new User(userCreateRequestDto, encodedPassword, profileStoredFilename));
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
        return new UserInfoResponseDto(userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required")));
    }

    @Transactional
    public UserInfoResponseDto updateInfo(Long userId, @Valid UserNicknameUpdateRequestDto userNicknameUpdateRequestDto, MultipartFile profileImage){
        List<ErrorInfoDto> errorInfoDtoList = new ArrayList<>();

        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required"));
        if(userNicknameUpdateRequestDto.getNickname()==null && profileImage==null) {
            throw new BadRequestException("invalid_request");
        }
        if(userNicknameUpdateRequestDto.getNickname()!=null && !user.getNickname().equals(userNicknameUpdateRequestDto.getNickname()) && userRepository.existsByNickname(userNicknameUpdateRequestDto.getNickname())){
            errorInfoDtoList.add(new ErrorInfoDto("nickname", "중복된 닉네임입니다."));
        }
        if(!errorInfoDtoList.isEmpty()){
            throw new BadRequestException("invalid_request", new ErrorResponseDto(errorInfoDtoList));
        }

        String oldProfileStoredFilename = user.getProfileStoredFilename();
        String newProfileStoredFilename = null;

        try {
            if (userNicknameUpdateRequestDto.getNickname() != null) {
                user.changeNickname(userNicknameUpdateRequestDto.getNickname());
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
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required"));

        user.changePassword(passwordEncoder.encode(userPasswordUpdateRequestDto.getPassword()));
        refreshTokenRepository.deleteByUserId(userId);
        user.changeUpdatedAt(LocalDateTime.now());
    }

    @Transactional
    public void logout(Long userId) {
        userRepository.findByUserIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UnauthorizedException("login_required"));

        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public void withdrawal(Long userId){
        User user = userRepository.findByUserIdAndDeletedAtIsNull(userId).orElseThrow(() -> new UnauthorizedException("login_required"));

        String profileStoredFilename = user.getProfileStoredFilename();

        String deletedEmail = "deleted_" + userId + "@delete.invalid";
        String deletedNickname = "탈퇴" + userId;
        String deletedPassword = UUID.randomUUID().toString();

        user.withdraw(deletedEmail, deletedNickname, deletedPassword);
        refreshTokenRepository.deleteByUserId(userId);
        userRepository.flush();

        if(!DEFAULT_PROFILE_IMAGE.equals(profileStoredFilename)){
            profileImageStorage.delete(profileStoredFilename);
        }
    }
}
