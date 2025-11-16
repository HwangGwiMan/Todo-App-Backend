package com.TodoApp.backend.domain.todo.dto;

import com.TodoApp.backend.domain.todo.entity.Todo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "TODO 생성/수정 요청")
public class TodoRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    @Schema(description = "TODO 제목", example = "Spring Boot 공부하기")
    private String title;

    @Schema(description = "TODO 설명", example = "JPA와 Security 챕터 복습", nullable = true, types = {"string", "null"})
    @Nullable
    private String description;

    @Schema(description = "TODO 상태", example = "TODO", allowableValues = {"TODO", "IN_PROGRESS", "DONE"}, nullable = true, types = {"string", "null"})
    @Nullable
    private Todo.TodoStatus status;

    @Schema(description = "우선순위", example = "MEDIUM", allowableValues = {"HIGH", "MEDIUM", "LOW"}, nullable = true, types = {"string", "null"})
    @Nullable
    private Todo.Priority priority;

    @Schema(description = "마감일", example = "2025-12-31T23:59:59", nullable = true, types = {"string", "null"})
    @Nullable
    private Timestamp dueDate;

    @Schema(description = "정렬 순서", example = "0")
    private Integer position;

    // Phase 2
    @Schema(description = "프로젝트 ID (Phase 2)", example = "1", nullable = true, types = {"integer", "null"})
    @Nullable
    private Long projectId;
}

