package org.example.communityservice.service;

import io.jsonwebtoken.JwtException;
import org.example.communityservice.common.exception.BadRequestException;
import org.example.communityservice.common.exception.UnauthorizedException;
import org.example.communityservice.common.security.JwtProvider;
import org.example.communityservice.common.security.RefreshTokenHasher;
import org.example.communityservice.dto.token.TokenResultDto;
import org.example.communityservice.dto.user.request.UserCreateRequestDto;
import org.example.communityservice.dto.user.request.UserInfoUpdateRequestDto;
import org.example.communityservice.dto.user.request.UserLoginRequestDto;
import org.example.communityservice.dto.user.request.UserPasswordUpdateRequestDto;
import org.example.communityservice.dto.user.response.UserInfoResponseDto;
import org.example.communityservice.dto.user.response.UserLoginResultDto;
import org.example.communityservice.entity.RefreshToken;
import org.example.communityservice.entity.User;
import org.example.communityservice.repository.RefreshTokenRepository;
import org.example.communityservice.repository.UserRepository;
import org.example.communityservice.storage.ProfileImageStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RefreshTokenRevoker refreshTokenRevoker;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenHasher refreshTokenHasher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProfileImageStorage profileImageStorage;

    @InjectMocks
    private UserService userService;

    @Test
    void 로그인에_성공한다() {
        Long userId = 3L;
        UserLoginRequestDto request = new UserLoginRequestDto("test@example.com", "Test1234!");
        User user = mock(User.class);

        when(user.getUserId()).thenReturn(userId);
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getPassword()).thenReturn("encoded-password");
        when(user.getNickname()).thenReturn("테스터");
        when(user.getProfileStoredFilename()).thenReturn("test_profile_img");
        when(userRepository.findByEmailAndDeletedAtIsNull(request.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .thenReturn(true);
        when(jwtProvider.createAccessToken(userId)).thenReturn("access-token");
        when(jwtProvider.createRefreshToken(userId)).thenReturn("refresh-token");
        when(refreshTokenHasher.hash("refresh-token")).thenReturn("hashed-refresh-token");
        when(jwtProvider.getAccessTokenValidityInMilliseconds()).thenReturn(300_000L);
        when(jwtProvider.getRefreshTokenValidityInSeconds()).thenReturn(604_800L);

        UserLoginResultDto result = userService.login(request);

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(userRepository).findByEmailAndDeletedAtIsNull("test@example.com");
        verify(passwordEncoder).matches("Test1234!", "encoded-password");
        verify(jwtProvider).createAccessToken(userId);
        verify(jwtProvider).createRefreshToken(userId);
        verify(refreshTokenHasher).hash("refresh-token");
        verify(refreshTokenRepository).deleteByUserId(userId);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();
        assertThat(savedRefreshToken.getToken()).isEqualTo("hashed-refresh-token");
        assertThat(savedRefreshToken.getUserId()).isEqualTo(userId);
        assertThat(savedRefreshToken.getExpiresAt()).isAfter(java.time.LocalDateTime.now());

        assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(result.getUserLoginResponseDto().getUserId()).isEqualTo(userId);
        assertThat(result.getUserLoginResponseDto().getEmail()).isEqualTo("test@example.com");
        assertThat(result.getUserLoginResponseDto().getNickname()).isEqualTo("테스터");
        assertThat(result.getUserLoginResponseDto().getProfileImage())
                .isEqualTo("/images/profiles/test_profile_img");
        assertThat(result.getUserLoginResponseDto().getToken().getAccessToken())
                .isEqualTo("access-token");
        assertThat(result.getUserLoginResponseDto().getToken().getExpiresIn())
                .isEqualTo(300_000L);
    }

    @Test
    void 일치하는_이메일_정보가_없으면_로그인에_실패한다(){
        UserLoginRequestDto request = new UserLoginRequestDto("wrong@example.com", "Test1234!");

        when(userRepository.findByEmailAndDeletedAtIsNull(request.getEmail()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("login_failed");

        verify(userRepository).findByEmailAndDeletedAtIsNull("wrong@example.com");
        verifyNoInteractions(
                passwordEncoder,
                jwtProvider,
                refreshTokenHasher,
                refreshTokenRepository
        );
    }

    @Test
    void 비밀번호가_일치하지_않으면_로그인에_실패한다(){
        User user = mock(User.class);
        UserLoginRequestDto request = new UserLoginRequestDto("test@example.com", "Test1234!");

        when(user.getPassword()).thenReturn("encoded-password");
        when(userRepository.findByEmailAndDeletedAtIsNull(request.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("login_failed");

        verify(userRepository).findByEmailAndDeletedAtIsNull("test@example.com");
        verify(passwordEncoder).matches("Test1234!", "encoded-password");

        verifyNoInteractions(
                jwtProvider,
                refreshTokenHasher,
                refreshTokenRepository
        );
    }

    @Test
    void 엑세스_토큰_발급에_성공한다(){
        String requestRefreshToken = "request_refresh_token";
        String requestRefreshTokenHash = "request_refresh_token_hash";
        String newAccessToken = "new_access_token";
        String newRefreshToken = "new_refresh_token";
        String newRefreshTokenHash = "new_refresh_token_hash";
        Long userId = 3L;

        RefreshToken savedRefreshToken = new RefreshToken(
                requestRefreshTokenHash,
                userId,
                LocalDateTime.now().plusDays(1)
        );
        User user = mock(User.class);

        when(refreshTokenHasher.hash(requestRefreshToken))
                .thenReturn(requestRefreshTokenHash);
        when(refreshTokenRepository.findByToken(requestRefreshTokenHash))
                .thenReturn(Optional.of(savedRefreshToken));
        when(jwtProvider.getRefreshTokenUserId(requestRefreshToken))
                .thenReturn(userId);
        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));
        when(user.getUserId()).thenReturn(userId);
        when(jwtProvider.createAccessToken(userId))
                .thenReturn(newAccessToken);
        when(jwtProvider.createRefreshToken(userId))
                .thenReturn(newRefreshToken);
        when(refreshTokenHasher.hash(newRefreshToken))
                .thenReturn(newRefreshTokenHash);
        when(jwtProvider.getAccessTokenValidityInMilliseconds())
                .thenReturn(300_000L);
        when(jwtProvider.getRefreshTokenValidityInSeconds())
                .thenReturn(604_800L);

        TokenResultDto result = userService.refreshAccessToken(requestRefreshToken);

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenHasher).hash(requestRefreshToken);
        verify(refreshTokenRepository).findByToken(requestRefreshTokenHash);
        verify(jwtProvider).getRefreshTokenUserId(requestRefreshToken);
        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
        verify(jwtProvider).createAccessToken(userId);
        verify(jwtProvider).createRefreshToken(userId);
        verify(refreshTokenHasher).hash(newRefreshToken);
        verify(refreshTokenRepository).delete(savedRefreshToken);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        verifyNoInteractions(refreshTokenRevoker);

        RefreshToken rotatedRefreshToken = refreshTokenCaptor.getValue();
        assertThat(rotatedRefreshToken.getToken()).isEqualTo(newRefreshTokenHash);
        assertThat(rotatedRefreshToken.getUserId()).isEqualTo(userId);
        assertThat(rotatedRefreshToken.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(result.getTokenInfoDto().getAccessToken()).isEqualTo(newAccessToken);
        assertThat(result.getTokenInfoDto().getExpiresIn()).isEqualTo(300_000L);
        assertThat(result.getNewRefreshToken()).isEqualTo(newRefreshToken);
    }

    @Test
    void 리프레시_토큰이_비어있으면_엑세스_토큰_발급에_실패한다(){
        assertThatThrownBy(() -> userService.refreshAccessToken(" "))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("unauthorized");

        verifyNoInteractions(
                refreshTokenHasher,
                refreshTokenRepository,
                refreshTokenRevoker,
                jwtProvider,
                userRepository
        );
    }

    @Test
    void 리프레시_토큰이_null이면_엑세스_토큰_발급에_실패한다(){
        assertThatThrownBy(() -> userService.refreshAccessToken(null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("unauthorized");

        verifyNoInteractions(
                refreshTokenHasher,
                refreshTokenRepository,
                refreshTokenRevoker,
                jwtProvider,
                userRepository
        );
    }

    @Test
    void 저장된_리프레시_토큰이_없으면_엑세스_토큰_발급에_실패한다(){
        String requestRefreshToken = "request_refresh_token";
        String refreshTokenHash = "refresh_token_hash";

        when(refreshTokenHasher.hash(requestRefreshToken))
                .thenReturn(refreshTokenHash);
        when(refreshTokenRepository.findByToken(refreshTokenHash))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.refreshAccessToken(requestRefreshToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("unauthorized");

        verify(refreshTokenHasher).hash(requestRefreshToken);
        verify(refreshTokenRepository).findByToken(refreshTokenHash);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        verifyNoInteractions(
                refreshTokenRevoker,
                jwtProvider,
                userRepository
        );
    }

    @Test
    void 리프레시_토큰이_만료되면_엑세스_토큰_발급에_실패한다(){
        String requestRefreshToken = "request_refresh_token";
        String refreshTokenHash = "refresh_token_hash";

        RefreshToken expiredRefreshToken = new RefreshToken(
                refreshTokenHash,
                3L,
                LocalDateTime.now().minusSeconds(1)
        );

        when(refreshTokenHasher.hash(requestRefreshToken))
                .thenReturn(refreshTokenHash);
        when(refreshTokenRepository.findByToken(refreshTokenHash))
                .thenReturn(Optional.of(expiredRefreshToken));

        assertThatThrownBy(() -> userService.refreshAccessToken(requestRefreshToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("unauthorized");

        verify(refreshTokenHasher).hash(requestRefreshToken);
        verify(refreshTokenRepository).findByToken(refreshTokenHash);
        verify(refreshTokenRevoker).revokeHash(refreshTokenHash);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        verifyNoInteractions(
                jwtProvider,
                userRepository
        );
    }

    @Test
    void 리프레시_토큰에서_사용자_ID_추출에_실패하면_토큰을_폐기하고_재발급에_실패한다(){
        JwtException jwtException = new JwtException("invalid refresh token");

        String requestRefreshToken = "request_refresh_token";
        String refreshTokenHash = "refresh_token_hash";

        RefreshToken refreshToken = new RefreshToken(
                refreshTokenHash,
                3L,
                LocalDateTime.now().plusDays(1)
        );

        when(refreshTokenHasher.hash(requestRefreshToken))
                .thenReturn(refreshTokenHash);
        when(refreshTokenRepository.findByToken(refreshTokenHash))
                .thenReturn(Optional.of(refreshToken));
        when(jwtProvider.getRefreshTokenUserId(requestRefreshToken))
                .thenThrow(jwtException);

        assertThatThrownBy(() -> userService.refreshAccessToken(requestRefreshToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("unauthorized");

        verify(refreshTokenHasher).hash(requestRefreshToken);
        verify(refreshTokenRepository).findByToken(refreshTokenHash);
        verify(jwtProvider).getRefreshTokenUserId(requestRefreshToken);
        verify(refreshTokenRevoker).revokeHash(refreshTokenHash);
        verify(userRepository, never()).findByUserIdAndDeletedAtIsNull(anyLong());
        verify(jwtProvider, never()).createAccessToken(anyLong());
        verify(jwtProvider, never()).createRefreshToken(anyLong());
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void 리프레시_토큰의_사용자_ID가_저장된_사용자_ID와_다르면_토큰을_폐기하고_재발급에_실패한다() {
        String requestRefreshToken = "request_refresh_token";
        String refreshTokenHash = "refresh_token_hash";
        Long savedUserId = 3L;
        Long tokenUserId = 4L;
        RefreshToken savedRefreshToken = new RefreshToken(
                refreshTokenHash,
                savedUserId,
                LocalDateTime.now().plusDays(1)
        );

        when(refreshTokenHasher.hash(requestRefreshToken))
                .thenReturn(refreshTokenHash);
        when(refreshTokenRepository.findByToken(refreshTokenHash))
                .thenReturn(Optional.of(savedRefreshToken));
        when(jwtProvider.getRefreshTokenUserId(requestRefreshToken))
                .thenReturn(tokenUserId);

        assertThatThrownBy(() -> userService.refreshAccessToken(requestRefreshToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("unauthorized");

        verify(refreshTokenHasher).hash(requestRefreshToken);
        verify(refreshTokenRepository).findByToken(refreshTokenHash);
        verify(jwtProvider).getRefreshTokenUserId(requestRefreshToken);
        verify(refreshTokenRevoker).revokeHash(refreshTokenHash);
        verify(userRepository, never()).findByUserIdAndDeletedAtIsNull(anyLong());
        verify(jwtProvider, never()).createAccessToken(anyLong());
        verify(jwtProvider, never()).createRefreshToken(anyLong());
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void 리프레시_토큰의_사용자가_존재하지_않으면_재발급에_실패한다() {
        String requestRefreshToken = "request_refresh_token";
        String refreshTokenHash = "refresh_token_hash";
        Long userId = 999L;

        RefreshToken savedRefreshToken = new RefreshToken(
                refreshTokenHash,
                userId,
                LocalDateTime.now().plusDays(1)
        );

        when(refreshTokenHasher.hash(requestRefreshToken))
                .thenReturn(refreshTokenHash);
        when(refreshTokenRepository.findByToken(refreshTokenHash))
                .thenReturn(Optional.of(savedRefreshToken));
        when(jwtProvider.getRefreshTokenUserId(requestRefreshToken))
                .thenReturn(userId);
        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.refreshAccessToken(requestRefreshToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("unauthorized");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
        verify(jwtProvider, never()).createAccessToken(anyLong());
        verify(jwtProvider, never()).createRefreshToken(anyLong());
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void 이미지_없이_회원가입하면_비밀번호를_암호화하고_기본_이미지를_사용한다() {
        UserCreateRequestDto request = new UserCreateRequestDto(
                "test@example.com",
                "Test1234!",
                "테스터"
        );

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);
        when(userRepository.existsByNickname(request.getNickname()))
                .thenReturn(false);
        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded-password");

        userService.createUser(request, null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());
        verify(userRepository).flush();
        verify(profileImageStorage, never()).store(any());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("테스터");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getProfileStoredFilename()).isEqualTo("test_image.png");
    }


    @Test
    void 이미지_포함해_회원가입하면_비밀번호를_암호화하고_업로드된_이미지를_사용한다() {
        UserCreateRequestDto request = new UserCreateRequestDto(
                "test@example.com",
                "Test1234!",
                "테스터"
        );

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);
        when(userRepository.existsByNickname(request.getNickname()))
                .thenReturn(false);
        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded-password");

        MultipartFile testProfileImg = mock(MultipartFile.class);
        when(testProfileImg.isEmpty()).thenReturn(false);
        when(profileImageStorage.store(testProfileImg))
                .thenReturn("test_profile_img");

        userService.createUser(request, testProfileImg);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());
        verify(userRepository).flush();
        verify(profileImageStorage).store(testProfileImg);
        verify(profileImageStorage, never()).delete("test_profile_img");

        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("테스터");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getProfileStoredFilename()).isEqualTo("test_profile_img");
    }

    @Test
    void 이메일이_중복되면_회원가입에_실패한다() {
        UserCreateRequestDto request = new UserCreateRequestDto(
                "duplicate@example.com",
                "Test1234!",
                "테스터"
        );

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);
        when(userRepository.existsByNickname(request.getNickname()))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.createUser(request, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("invalid_request");

        verify(passwordEncoder, never()).encode(anyString());
        verify(profileImageStorage, never()).store(any());
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).flush();
    }

    @Test
    void 닉네임이_중복되면_회원가입에_실패한다() {
        UserCreateRequestDto request = new UserCreateRequestDto(
                "duplicate@example.com",
                "Test1234!",
                "테스터"
        );

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);
        when(userRepository.existsByNickname(request.getNickname()))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("invalid_request");

        verify(passwordEncoder, never()).encode(anyString());
        verify(profileImageStorage, never()).store(any());
        verify(userRepository, never()).save(any(User.class));
        verify(userRepository, never()).flush();
    }

    @Test
    void 사용자_저장에_실패하면_저장했던_이미지_삭제한다() {
        UserCreateRequestDto request = new UserCreateRequestDto(
                "save-failure@example.com",
                "Test1234!",
                "테스터"
        );
        RuntimeException saveException = new RuntimeException("사용자 저장 실패");

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);
        when(userRepository.existsByNickname(request.getNickname()))
                .thenReturn(false);
        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded-password");

        MultipartFile testProfileImg = mock(MultipartFile.class);
        when(testProfileImg.isEmpty()).thenReturn(false);
        when(profileImageStorage.store(testProfileImg))
                .thenReturn("test_profile_img");
        when(userRepository.save(any(User.class)))
                .thenThrow(saveException);

        assertThatThrownBy(() -> userService.createUser(request, testProfileImg))
                .isSameAs(saveException);

        verify(profileImageStorage).store(testProfileImg);
        verify(profileImageStorage).delete("test_profile_img");
        verify(userRepository).save(any(User.class));
        verify(userRepository, never()).flush();
    }

    @Test
    void 사용자_정보_조회에_성공한다(){
        Long userId = 3L;
        User user = new User(
                new UserCreateRequestDto(
                        "test@example.com",
                        "Test1234!",
                        "테스터"
                ),
                "encoded-password",
                "test_profile_img"
        );

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        UserInfoResponseDto response = userService.showInfo(userId);

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);

        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("테스터");
        assertThat(response.getProfileImage())
                .isEqualTo("/images/profiles/test_profile_img");
    }

    @Test
    void 사용자_정보_조회에_실패한다(){
        Long userId = 6L;

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.showInfo(userId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("login_required");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
    }

    @Test
    void 이메일과_닉네임_변경에_성공한다() {
        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto("new@example.com", "new 테스터");

        Long userId = 3L;
        User user = new User(
                new UserCreateRequestDto(
                        "old@example.com",
                        "Test1234!",
                        "old 테스터"
                ),
                "encoded-password",
                "old_profile_img"
        );

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);
        when(userRepository.existsByNickname(request.getNickname()))
                .thenReturn(false);

        UserInfoResponseDto response = userService.updateInfo(userId, request, null);

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).existsByNickname("new 테스터");
        verify(profileImageStorage, never()).store(any());
        verify(userRepository).flush();
        verify(profileImageStorage, never()).delete(anyString());

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getNickname()).isEqualTo("new 테스터");
        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getNickname()).isEqualTo("new 테스터");
    }

    @Test
    void 이메일이_중복되면_회원정보_수정에_실패한다(){
        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto("duplicate@example.com", null);

        Long userId = 3L;
        User user = new User(
                new UserCreateRequestDto(
                        "old@example.com",
                        "Test1234!",
                        "old 테스터"
                ),
                "encoded-password",
                "old_profile_img"
        );

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.updateInfo(userId, request, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("invalid_request");
        assertThat(user.getEmail()).isEqualTo("old@example.com");
        assertThat(user.getNickname()).isEqualTo("old 테스터");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).existsByNickname(anyString());
        verify(profileImageStorage, never()).store(any());
        verify(userRepository, never()).flush();
        verify(profileImageStorage, never()).delete(anyString());
    }

    @Test
    void 닉네임이_중복되면_회원정보_수정에_실패한다(){
        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto(null, "중복닉네임");

        Long userId = 3L;
        User user = new User(
                new UserCreateRequestDto(
                        "old@example.com",
                        "Test1234!",
                        "old 테스터"
                ),
                "encoded-password",
                "old_profile_img"
        );

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));
        when(userRepository.existsByNickname(request.getNickname()))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.updateInfo(userId, request, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("invalid_request");
        assertThat(user.getEmail()).isEqualTo("old@example.com");
        assertThat(user.getNickname()).isEqualTo("old 테스터");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
        verify(userRepository, never()).existsByEmail(request.getEmail());
        verify(userRepository).existsByNickname(request.getNickname());
        verify(profileImageStorage, never()).store(any());
        verify(userRepository, never()).flush();
        verify(profileImageStorage, never()).delete(anyString());
    }

    @Test
    void 프로필_이미지_변경에_성공한다(){
        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto();
        MultipartFile newProfileImg = mock(MultipartFile.class);

        Long userId = 3L;
        User user = new User(
                new UserCreateRequestDto(
                        "test@example.com",
                        "Test1234!",
                        "테스터"
                ),
                "encoded-password",
                "old_profile_img"
        );

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));
        when(newProfileImg.isEmpty()).thenReturn(false);
        when(profileImageStorage.store(newProfileImg))
                .thenReturn("new_profile_img");

        UserInfoResponseDto response = userService.updateInfo(userId, request, newProfileImg);

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
        verify(profileImageStorage).store(newProfileImg);
        verify(userRepository).flush();
        verify(profileImageStorage).delete("old_profile_img");

        assertThat(user.getProfileStoredFilename()).isEqualTo("new_profile_img");
        assertThat(response.getProfileImage()).isEqualTo("/images/profiles/new_profile_img");
    }

    @Test
    void 프로필_이미지_변경에_실패하면_새로_저장된_이미지만_삭제한다(){
        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto();
        MultipartFile newProfileImg = mock(MultipartFile.class);
        RuntimeException flushException = new RuntimeException("사용자 정보 저장 실패");

        Long userId = 3L;
        User user = new User(
                new UserCreateRequestDto(
                        "test@example.com",
                        "Test1234!",
                        "테스터"
                ),
                "encoded-password",
                "old_profile_img"
        );

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));
        when(newProfileImg.isEmpty()).thenReturn(false);
        when(profileImageStorage.store(newProfileImg))
                .thenReturn("new_profile_img");

        doThrow(flushException).when(userRepository).flush();

        assertThatThrownBy(()-> userService.updateInfo(userId, request, newProfileImg))
                .isSameAs(flushException);

        verify(profileImageStorage).store(newProfileImg);
        verify(profileImageStorage).delete("new_profile_img");
        verify(profileImageStorage, never()).delete("old_profile_img");
    }

    @Test
    void 변경할_사용자_정보가_없으면_회원정보_수정에_실패한다(){
        UserInfoUpdateRequestDto request = new UserInfoUpdateRequestDto();
        Long userId = 3L;
        User user = new User(
                new UserCreateRequestDto(
                        "test@example.com",
                        "Test1234!",
                        "테스터"
                ),
                "encoded-password",
                "old_profile_img"
        );

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));
        assertThatThrownBy(() -> userService.updateInfo(userId, request, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("invalid_request");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
        verify(profileImageStorage,never()).store(any());
        verify(userRepository, never()).flush();
        verify(profileImageStorage, never()).delete(anyString());
    }

    @Test
    void 비밀번호_변경에_성공한다(){
        Long userId = 3L;
        User user = new User(
                new UserCreateRequestDto(
                        "test@example.com",
                        "Test1234!",
                        "테스터"
                ),
                "old-encoded-password",
                "test_profile_img"
        );

        UserPasswordUpdateRequestDto updateRequest = new UserPasswordUpdateRequestDto("Test123@");

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode(updateRequest.getPassword()))
                .thenReturn("new-encoded-password");

        userService.updatePassword(userId, updateRequest);

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
        verify(passwordEncoder).encode("Test123@");
        verify(refreshTokenRepository).deleteByUserId(userId);

        assertThat(user.getPassword()).isEqualTo("new-encoded-password");
    }

    @Test
    void 존재하지_않는_사용자의_비밀번호_변경은_실패한다(){
        Long userId = 999L;

        UserPasswordUpdateRequestDto updateRequest = new UserPasswordUpdateRequestDto("Test123@");
        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(()-> userService.updatePassword(userId, updateRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("login_required");

        verify(passwordEncoder, never()).encode(anyString());
        verify(refreshTokenRepository, never()).deleteByUserId(anyLong());
    }

    @Test
    void 회원탈퇴_시_소프트_딜리트가_적용된다(){
        Long userId = 3L;
        User user = new User(
                new UserCreateRequestDto(
                        "test@example.com",
                        "Test1234!",
                        "테스터"
                ),
                "encoded-password",
                "test_profile_img"
        );

        String deletedEmail = "deleted_" + userId + "@delete.invalid";
        String deletedNickname = "탈퇴" + userId;

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        userService.withdrawal(userId);

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
        verify(refreshTokenRepository).deleteByUserId(userId);
        verify(userRepository).flush();
        verify(profileImageStorage).delete("test_profile_img");

        assertThat(user.getEmail()).isEqualTo(deletedEmail);
        assertThat(user.getNickname()).isEqualTo(deletedNickname);
        assertThat(user.getPassword()).isNotEqualTo("encoded-password");
        assertThat(user.getProfileStoredFilename()).isEqualTo("test_image.png");
        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    void 회원탈퇴_시_기본_프로필_이미지는_삭제하지_않는다() {
        Long userId = 3L;
        User user = new User(
                new UserCreateRequestDto(
                        "test@example.com",
                        "Test1234!",
                        "테스터"
                ),
                "encoded-password",
                null
        );

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        userService.withdrawal(userId);

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
        verify(refreshTokenRepository).deleteByUserId(userId);
        verify(userRepository).flush();
        verify(profileImageStorage, never()).delete(anyString());

        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.getProfileStoredFilename()).isEqualTo("test_image.png");
    }

    @Test
    void 존재하지_않는_사용자는_회원탈퇴에_실패한다() {
        Long userId = 999L;

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.withdrawal(userId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("login_required");

        verify(refreshTokenRepository, never()).deleteByUserId(anyLong());
        verify(userRepository, never()).flush();
        verify(profileImageStorage, never()).delete(anyString());
    }

    @Test
    void 로그아웃에_성공한다(){
        Long userId = 3L;
        User user = mock(User.class);

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        userService.logout(userId);

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
        verify(refreshTokenRepository).deleteByUserId(userId);
    }

    @Test
    void 존재하지_않는_사용자는_로그아웃에_실패한다(){
        Long userId = 999L;

        when(userRepository.findByUserIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.logout(userId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("login_required");

        verify(userRepository).findByUserIdAndDeletedAtIsNull(userId);
        verify(refreshTokenRepository, never()).deleteByUserId(userId);
    }
}
