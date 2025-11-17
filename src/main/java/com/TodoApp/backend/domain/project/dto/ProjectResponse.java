package com.TodoApp.backend.domain.project.dto;

import com.TodoApp.backend.domain.project.entity.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.sql.Timestamp;

/**
 * 프로젝트 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    @Schema(
            description = "프로젝트 ID",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "프로젝트명",
            example = "개인 프로젝트"
    )
    private String name;

    @Schema(
            description = "프로젝트 설명",
            example = "개인적으로 진행하는 프로젝트들",
            nullable = true,
            types = {"string", "null"}
    )
    @Nullable
    private String description;

    @Schema(
            description = "프로젝트 색상 (HEX 코드)",
            example = "#3B82F6"
    )
    private String color;

    @Schema(
            description = "기본 프로젝트 여부",
            example = "false"
    )
    private Boolean isDefault;

    @Schema(
            description = "정렬 순서",
            example = "0"
    )
    private Integer position;

    @Schema(
            description = "생성일시",
            example = "2024-11-17T10:30:00.000Z"
    )
    private Timestamp createdAt;

    @Schema(
            description = "수정일시",
            example = "2024-11-17T10:30:00.000Z"
    )
    private Timestamp updatedAt;

    @Schema(
            description = "프로젝트 내 TODO 개수",
            example = "5"
    )
    private Long todoCount;

    /**
     * Project 엔티티를 ProjectResponse로 변환
     */
    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .color(project.getColor())
                .isDefault(project.getIsDefault())
                .position(project.getPosition())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    /**
     * Project 엔티티와 TODO 개수를 포함한 ProjectResponse로 변환
     */
    public static ProjectResponse fromWithTodoCount(Project project, Long todoCount) {
        ProjectResponse response = from(project);
        response.setTodoCount(todoCount);
        return response;
    }
}
