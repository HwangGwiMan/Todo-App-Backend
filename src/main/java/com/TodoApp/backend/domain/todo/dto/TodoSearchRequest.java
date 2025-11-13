package com.TodoApp.backend.domain.todo.dto;

import com.TodoApp.backend.domain.todo.entity.Todo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "TODO 검색/필터링 요청")
public class TodoSearchRequest {

    @Schema(description = "검색 키워드 (제목, 설명)", example = "Spring")
    private String keyword;

    @Schema(description = "상태 필터", example = "TODO", allowableValues = {"TODO", "IN_PROGRESS", "DONE"})
    private Todo.TodoStatus status;

    @Schema(description = "우선순위 필터", example = "HIGH", allowableValues = {"HIGH", "MEDIUM", "LOW"})
    private Todo.Priority priority;

    @Schema(description = "마감일 시작 범위", example = "2025-11-01T00:00:00")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dueDateStart;

    @Schema(description = "마감일 종료 범위", example = "2025-11-30T23:59:59")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dueDateEnd;

    @Schema(description = "정렬 필드", example = "createdAt", allowableValues = {"createdAt", "dueDate", "priority", "position", "title"})
    @Builder.Default
    private String sortBy = "createdAt";

    @Schema(description = "정렬 방향", example = "DESC", allowableValues = {"ASC", "DESC"})
    @Builder.Default
    private String sortDirection = "DESC";

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "50")
    @Builder.Default
    private Integer size = 50;

    // Phase 2
    @Schema(description = "프로젝트 ID 필터 (Phase 2)", example = "1")
    private Long projectId;
}

