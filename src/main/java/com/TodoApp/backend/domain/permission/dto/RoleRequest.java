package com.TodoApp.backend.domain.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 역할 생성/수정 요청 DTO
 */
@Getter
@Setter
@Schema(description = "역할 생성/수정 요청")
public class RoleRequest {
    
    @NotBlank(message = "역할명은 필수입니다")
    @Size(min = 1, max = 50, message = "역할명은 1-50자여야 합니다")
    @Schema(description = "역할명", example = "MANAGER", required = true)
    private String name;
    
    @Size(max = 255, message = "설명은 255자 이하여야 합니다")
    @Schema(description = "역할 설명", example = "프로젝트 관리자 역할")
    private String description;
    
    @Schema(description = "할당할 권한 ID 목록", example = "[1, 2, 3]")
    private List<Long> permissionIds;
}
