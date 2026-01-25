package com.TodoApp.backend.fixture.dto;

import com.TodoApp.backend.domain.auth.dto.SignupRequest;
import com.core.test.utils.TestSupport;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SignupRequest DTO 검증 테스트
 * Bean Validation을 활용한 DTO 검증 테스트
 */
@DisplayName("SignupRequest 검증 테스트")
class SignupRequestValidationTest {

    private static Validator validator;
    private static TestSupport<SignupRequest> signupRequestSupport;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        signupRequestSupport = new TestSupport<>(SignupRequest.class);
    }

    @Test
    @DisplayName("유효한 SignupRequest 생성")
    void 유효한_SignupRequest_생성() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("username이 null인 경우 검증 실패")
    void username_null_검증_실패() {
        // Given
        SignupRequest request = new SignupRequest();
        request.setUsername(null);
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    @DisplayName("username이 빈 문자열인 경우 검증 실패")
    void username_빈문자열_검증_실패() {
        // Given
        SignupRequest request = signupRequestSupport.monkey();
        request.setUsername("");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    @DisplayName("username이 너무 짧은 경우 검증 실패")
    void username_너무짧음_검증_실패() {
        // Given
        SignupRequest request = signupRequestSupport.monkey();
        request.setUsername("ab"); // 3자 미만
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    @DisplayName("email이 null인 경우 검증 실패")
    void email_null_검증_실패() {
        // Given
        SignupRequest request = signupRequestSupport.monkey();
        request.setUsername("testuser");
        request.setEmail(null);
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("email 형식이 잘못된 경우 검증 실패")
    void email_형식_잘못됨_검증_실패() {
        // Given
        SignupRequest request = signupRequestSupport.monkey();
        request.setUsername("testuser");
        request.setEmail("invalid-email");
        request.setPassword("password123");

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("password가 null인 경우 검증 실패")
    void password_null_검증_실패() {
        // Given
        SignupRequest request = signupRequestSupport.monkey();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword(null);

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("password가 너무 짧은 경우 검증 실패")
    void password_너무짧음_검증_실패() {
        // Given
        SignupRequest request = signupRequestSupport.monkey();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("12345"); // 6자 미만

        // When
        Set<ConstraintViolation<SignupRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}
