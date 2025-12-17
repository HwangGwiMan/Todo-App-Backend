package com.TodoApp.backend.global.common.dto;

import com.TodoApp.backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private boolean success;
    private int status;
    private String message;
    private String code;
    private LocalDateTime timestamp;
    
    /**
     * ErrorCode로부터 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .success(false)
                .status(errorCode.getStatus())
                .message(errorCode.getMessage())
                .code(errorCode.name())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * ErrorCode와 커스텀 메시지로 ErrorResponse 생성
     */
    public static ErrorResponse of(ErrorCode errorCode, String customMessage) {
        return ErrorResponse.builder()
                .success(false)
                .status(errorCode.getStatus())
                .message(customMessage)
                .code(errorCode.name())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * 상태 코드와 메시지로 ErrorResponse 생성
     */
    public static ErrorResponse of(int status, String message) {
        return ErrorResponse.builder()
                .success(false)
                .status(status)
                .message(message)
                .code("UNKNOWN")
                .timestamp(LocalDateTime.now())
                .build();
    }
}

