package com.TodoApp.backend.fixture.dto;

import com.TodoApp.backend.domain.todo.dto.TodoRequest;
import com.TodoApp.backend.domain.todo.entity.Todo;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.core.test.utils.TestSupport;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TodoRequest DTO 검증 테스트
 * Bean Validation을 활용한 DTO 검증 테스트
 */
@DisplayName("TodoRequest 검증 테스트")
class TodoRequestValidationTest {

    private static Validator validator;
    private static final TestSupport<TodoRequest> todoRequestSupport = new TestSupport<>(TodoRequest.class);

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 TodoRequest 생성")
    void 유효한_TodoRequest_생성() {
        // Given
        TodoRequest request = todoRequestSupport.monkey();
        request.setTitle("테스트 TODO");
        request.setDescription("테스트 설명");
        request.setStatus(Todo.TodoStatus.TODO);
        request.setPriority(Todo.Priority.MEDIUM);

        // When
        Set<ConstraintViolation<TodoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("title이 null인 경우 검증 실패")
    void title_null_검증_실패() {
        // Given
        TodoRequest request = todoRequestSupport.monkey();
        request.setTitle(null);
        request.setDescription("테스트 설명");

        // When
        Set<ConstraintViolation<TodoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    @DisplayName("title이 빈 문자열인 경우 검증 실패")
    void title_빈문자열_검증_실패() {
        // Given
        TodoRequest request = todoRequestSupport.monkey();
        request.setTitle("");
        request.setDescription("테스트 설명");

        // When
        Set<ConstraintViolation<TodoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    @DisplayName("title이 너무 긴 경우 검증 실패")
    void title_너무김_검증_실패() {
        // Given
        String longTitle = "a".repeat(256); // 255자 초과
        TodoRequest request = todoRequestSupport.monkey();
        request.setTitle(longTitle);
        request.setDescription("테스트 설명");

        // When
        Set<ConstraintViolation<TodoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    @DisplayName("description은 null이어도 유효")
    void description_null_유효() {
        // Given
        TodoRequest request = todoRequestSupport.monkey();
        request.setTitle("테스트 TODO");
        request.setDescription(null);

        // When
        Set<ConstraintViolation<TodoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("status는 null이어도 유효")
    void status_null_유효() {
        // Given
        TodoRequest request = todoRequestSupport.monkey();
        request.setTitle("테스트 TODO");
        request.setStatus(null);

        // When
        Set<ConstraintViolation<TodoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("priority는 null이어도 유효")
    void priority_null_유효() {
        // Given
        TodoRequest request = todoRequestSupport.monkey();
        request.setTitle("테스트 TODO");
        request.setPriority(null);

        // When
        Set<ConstraintViolation<TodoRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }
}
