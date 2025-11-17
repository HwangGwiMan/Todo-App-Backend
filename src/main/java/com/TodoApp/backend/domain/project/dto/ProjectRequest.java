package com.TodoApp.backend.domain.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * 프로젝트 생성/수정 요청 DTO
 */
@Data
@Getter
@Setter
public class ProjectRequest {

    @Schema(
            description = "프로젝트명",
            example = "개인 프로젝트",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "프로젝트명은 필수입니다")
    @Size(max = 100, message = "프로젝트명은 100자 이하여야 합니다")
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
            example = "#3B82F6",
            pattern = "^#[0-9A-Fa-f]{6}$"
    )
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 유효한 HEX 형식이어야 합니다 (예: #FF0000)")
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
}
