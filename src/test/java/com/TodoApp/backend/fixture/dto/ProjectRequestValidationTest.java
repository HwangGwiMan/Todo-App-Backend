package com.TodoApp.backend.fixture.dto;

import com.TodoApp.backend.domain.project.dto.ProjectRequest;
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
 * ProjectRequest DTO 검증 테스트
 * Bean Validation을 활용한 DTO 검증 테스트
 */
@DisplayName("ProjectRequest 검증 테스트")
class ProjectRequestValidationTest {

    private static Validator validator;
    private static TestSupport<ProjectRequest> projectRequestSupport;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        projectRequestSupport = new TestSupport<>(ProjectRequest.class);
    }

    @Test
    @DisplayName("유효한 ProjectRequest 생성")
    void 유효한_ProjectRequest_생성() {
        // Given
        ProjectRequest request = new ProjectRequest();
        request.setName("테스트 프로젝트");
        request.setDescription("테스트 설명");
        request.setColor("#3B82F6");
        request.setIsDefault(false);
        request.setPosition(0);

        // When
        Set<ConstraintViolation<ProjectRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("name이 null인 경우 검증 실패")
    void name_null_검증_실패() {
        // Given
        ProjectRequest request = projectRequestSupport.monkey();
        request.setName(null);
        request.setColor("#3B82F6");

        // When
        Set<ConstraintViolation<ProjectRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("name이 빈 문자열인 경우 검증 실패")
    void name_빈문자열_검증_실패() {
        // Given
        ProjectRequest request = projectRequestSupport.monkey();
        request.setName("");
        request.setColor("#3B82F6");

        // When
        Set<ConstraintViolation<ProjectRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("name이 너무 긴 경우 검증 실패")
    void name_너무김_검증_실패() {
        // Given
        String longName = "a".repeat(101); // 100자 초과
        ProjectRequest request = projectRequestSupport.monkey();
        request.setName(longName);
        request.setColor("#3B82F6");

        // When
        Set<ConstraintViolation<ProjectRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @Test
    @DisplayName("color 형식이 잘못된 경우 검증 실패")
    void color_형식_잘못됨_검증_실패() {
        // Given
        ProjectRequest request = new ProjectRequest();
        request.setName("테스트 프로젝트");
        request.setColor("invalid-color"); // HEX 형식 아님

        // When
        Set<ConstraintViolation<ProjectRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("color"));
    }

    @Test
    @DisplayName("color가 유효한 HEX 형식인 경우 검증 통과")
    void color_유효한_HEX_형식_검증_통과() {
        // Given
        ProjectRequest request = new ProjectRequest();
        request.setName("테스트 프로젝트");
        request.setColor("#FF0000");

        // When
        Set<ConstraintViolation<ProjectRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("description은 null이어도 유효")
    void description_null_유효() {
        // Given
        ProjectRequest request = new ProjectRequest();
        request.setName("테스트 프로젝트");
        request.setDescription(null);
        request.setColor("#3B82F6");

        // When
        Set<ConstraintViolation<ProjectRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }
}
