package com.TodoApp.backend.domain.permission.dto;

import com.TodoApp.backend.domain.permission.entity.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 권한 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "권한 정보")
public class PermissionResponse {
    
    @Schema(description = "권한 ID", example = "1")
    private Long id;
    
    @Schema(description = "권한명", example = "TODO_READ")
    private String name;
    
    @Schema(description = "권한 설명", example = "TODO 조회")
    private String description;
    
    @Schema(description = "리소스", example = "TODO")
    private String resource;
    
    @Schema(description = "액션", example = "READ")
    private String action;
    
    public static PermissionResponse from(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .resource(permission.getResource().name())
                .action(permission.getAction().name())
                .build();
    }
}
