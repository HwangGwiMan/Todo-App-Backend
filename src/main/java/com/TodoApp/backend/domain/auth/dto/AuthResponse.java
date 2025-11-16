package com.TodoApp.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "인증 응답")
public class AuthResponse {

    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;
    
    @Builder.Default
    @Schema(description = "토큰 타입", example = "Bearer")
    private String type = "Bearer";
    
    @Schema(description = "사용자 이름", example = "홍길동")
    private String username;
    
    @Schema(description = "이메일", example = "hong@example.com")
    private String email;
    
    @Schema(description = "사용자 역할", example = "USER")
    private String role;

    public AuthResponse(String token, String username, String email, String role) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}

