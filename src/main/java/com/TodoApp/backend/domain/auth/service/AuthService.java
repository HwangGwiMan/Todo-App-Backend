package com.TodoApp.backend.domain.auth.service;

import com.TodoApp.backend.domain.auth.dto.AuthResponse;
import com.TodoApp.backend.domain.auth.dto.LoginRequest;
import com.TodoApp.backend.domain.auth.dto.SignupRequest;
import com.TodoApp.backend.domain.permission.entity.Role;
import com.TodoApp.backend.domain.permission.repository.RoleRepository;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.domain.user.repository.UserRepository;
import com.TodoApp.backend.global.exception.BusinessException;
import com.TodoApp.backend.global.exception.ErrorCode;
import com.TodoApp.backend.global.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // 사용자 이름 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME, "이미 사용 중인 이메일입니다");
        }

        // 기본 USER 역할 조회
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND, "기본 USER 역할이 존재하지 않습니다."));

        // 사용자 생성
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(user);

        // 역할 이름 목록 생성 (첫 번째 역할 사용)
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("USER");

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(roleName)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // 인증 처리
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 인증된 사용자 정보 가져오기
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(userDetails);

        // 역할 이름 목록 생성 (첫 번째 역할 사용)
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElse("USER");

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(roleName)
                .build();
    }
}

