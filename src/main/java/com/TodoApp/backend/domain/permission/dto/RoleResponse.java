package com.TodoApp.backend.domain.permission.dto;

import com.TodoApp.backend.domain.permission.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 역할 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "역할 정보")
public class RoleResponse {
    
    @Schema(description = "역할 ID", example = "1")
    private Long id;
    
    @Schema(description = "역할명", example = "USER")
    private String name;
    
    @Schema(description = "역할 설명", example = "일반 사용자 역할")
    private String description;
    
    @Schema(description = "할당된 권한 목록")
    private List<PermissionResponse> permissions;
    
    public static RoleResponse from(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions() != null 
                        ? role.getPermissions().stream()
                                .map(PermissionResponse::from)
                                .collect(Collectors.toList())
                        : List.of())
                .build();
    }
}
