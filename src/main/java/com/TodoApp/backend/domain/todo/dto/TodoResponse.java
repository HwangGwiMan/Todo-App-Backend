package com.TodoApp.backend.domain.todo.dto;

import com.TodoApp.backend.domain.todo.entity.Todo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "TODO 응답")
public class TodoResponse {

    @Schema(description = "TODO ID", example = "1")
    private Long id;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String username;

    @Schema(description = "TODO 제목", example = "Spring Boot 공부하기")
    private String title;

    @Schema(description = "TODO 설명", example = "JPA와 Security 챕터 복습")
    private String description;

    @Schema(description = "TODO 상태", example = "TODO")
    private String status;

    @Schema(description = "우선순위", example = "MEDIUM")
    private String priority;

    @Schema(description = "마감일", example = "2025-12-31T23:59:59")
    private LocalDateTime dueDate;

    @Schema(description = "완료일", example = "2025-11-10T14:30:00")
    private LocalDateTime completedAt;

    @Schema(description = "정렬 순서", example = "0")
    private Integer position;

    @Schema(description = "프로젝트 ID (Phase 2)", example = "1")
    private Long projectId;

    @Schema(description = "생성일", example = "2025-11-10T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2025-11-10T14:30:00")
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환
    public static TodoResponse from(Todo todo) {
        return TodoResponse.builder()
                .id(todo.getId())
                .userId(todo.getUser().getId())
                .username(todo.getUser().getUsername())
                .title(todo.getTitle())
                .description(todo.getDescription())
                .status(todo.getStatus() != null ? todo.getStatus().name() : null)
                .priority(todo.getPriority() != null ? todo.getPriority().name() : null)
                .dueDate(todo.getDueDate())
                .completedAt(todo.getCompletedAt())
                .position(todo.getPosition())
                .projectId(todo.getProjectId())
                .createdAt(todo.getCreatedAt())
                .updatedAt(todo.getUpdatedAt())
                .build();
    }
}

