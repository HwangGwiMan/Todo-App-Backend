package com.TodoApp.backend.domain.auth.service;

import com.TodoApp.backend.domain.auth.dto.AuthResponse;
import com.TodoApp.backend.domain.auth.dto.LoginRequest;
import com.TodoApp.backend.domain.auth.dto.SignupRequest;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.domain.user.repository.UserRepository;
import com.TodoApp.backend.global.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
// @SpringBootTest 제거 - Mockito 단위 테스트에서는 불필요
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(User.Role.USER)
                .build();
        testUser.setId(1L);

        signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signup_성공_테스트() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.signup(signupRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("USER");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 사용자명 중복")
    void signup_실패_사용자명_중복() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이미 존재하는 사용자명입니다.");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signup_실패_이메일_중복() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이미 사용 중인 이메일입니다");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_성공_테스트() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        // When
        AuthResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getType()).isEqualTo("Bearer");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo("USER");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtUtil).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("로그인 실패 - 인증 실패")
    void login_실패_인증_실패() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("인증 실패"));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("인증 실패");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(anyString());
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void login_실패_사용자_없음() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }
}

