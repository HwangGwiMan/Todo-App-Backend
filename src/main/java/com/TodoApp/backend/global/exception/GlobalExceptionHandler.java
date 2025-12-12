package com.TodoApp.backend.global.exception;

import com.TodoApp.backend.global.common.dto.ApiResponse;
import com.TodoApp.backend.global.common.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bean Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        // 첫 번째 에러 메시지를 메인 메시지로 사용
        String firstErrorMessage = errors.values().iterator().next();
        
        log.warn("Validation error: {}", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(firstErrorMessage, errors));
    }
    
    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: code={}, message={}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode());
        return ResponseEntity
                .status(ex.getStatus())
                .body(errorResponse);
    }
    
    /**
     * 접근 거부 예외 처리 (Spring Security)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.FORBIDDEN);
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }
    
    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_INPUT_VALUE, 
                ex.getMessage()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
    
    /**
     * 전역 예외 처리 (모든 예외의 최종 처리)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
    
    /* ========================================
     * 커스텀 예외 핸들러 추가 예시
     * ========================================
     * 
     * 1. ErrorCode enum에 새로운 에러 코드 추가:
     * 
     * // ErrorCode.java
     * FILE_UPLOAD_FAILED(400, "파일 업로드에 실패했습니다."),
     * FILE_SIZE_EXCEEDED(413, "파일 크기가 제한을 초과했습니다."),
     * 
     * 
     * 2. 특정 예외에 대한 핸들러 추가:
     * 
     * @ExceptionHandler(FileUploadException.class)
     * public ResponseEntity<ErrorResponse> handleFileUploadException(FileUploadException ex) {
     *     log.error("File upload failed: {}", ex.getMessage());
     *     
     *     ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.FILE_UPLOAD_FAILED);
     *     return ResponseEntity
     *             .status(HttpStatus.BAD_REQUEST)
     *             .body(errorResponse);
     * }
     * 
     * 
     * 3. 여러 예외를 하나의 핸들러로 처리:
     * 
     * @ExceptionHandler({
     *     MaxUploadSizeExceededException.class,
     *     FileSizeLimitExceededException.class
     * })
     * public ResponseEntity<ErrorResponse> handleFileSizeException(Exception ex) {
     *     log.warn("File size exceeded: {}", ex.getMessage());
     *     
     *     ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.FILE_SIZE_EXCEEDED);
     *     return ResponseEntity
     *             .status(HttpStatus.PAYLOAD_TOO_LARGE)
     *             .body(errorResponse);
     * }
     * 
     * 
     * 4. 커스텀 메시지와 함께 에러 반환:
     * 
     * @ExceptionHandler(DataIntegrityViolationException.class)
     * public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
     *         DataIntegrityViolationException ex) {
     *     log.error("Data integrity violation: {}", ex.getMessage());
     *     
     *     String customMessage = "데이터 무결성 제약 조건을 위반했습니다.";
     *     ErrorResponse errorResponse = ErrorResponse.of(
     *         ErrorCode.INVALID_INPUT_VALUE, 
     *         customMessage
     *     );
     *     return ResponseEntity
     *             .status(HttpStatus.BAD_REQUEST)
     *             .body(errorResponse);
     * }
     * 
     * 
     * 5. Spring Security 인증 예외 처리:
     * 
     * @ExceptionHandler(AuthenticationException.class)
     * public ResponseEntity<ErrorResponse> handleAuthenticationException(
     *         AuthenticationException ex) {
     *     log.warn("Authentication failed: {}", ex.getMessage());
     *     
     *     ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.INVALID_CREDENTIALS);
     *     return ResponseEntity
     *             .status(HttpStatus.UNAUTHORIZED)
     *             .body(errorResponse);
     * }
     * 
     ======================================== */
}