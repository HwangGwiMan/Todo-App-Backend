package com.TodoApp.backend.domain.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 사용자 역할 할당 요청 DTO
 */
@Getter
@Setter
@Schema(description = "사용자 역할 할당 요청")
public class UserRoleRequest {
    
    @NotEmpty(message = "역할 ID 목록은 필수입니다")
    @Schema(description = "할당할 역할 ID 목록", example = "[1, 2]", required = true)
    private List<Long> roleIds;
}
