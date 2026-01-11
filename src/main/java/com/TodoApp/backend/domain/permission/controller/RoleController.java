package com.TodoApp.backend.domain.permission.controller;

import com.TodoApp.backend.domain.permission.dto.RoleRequest;
import com.TodoApp.backend.domain.permission.dto.RoleResponse;
import com.TodoApp.backend.domain.permission.service.RoleService;
import com.TodoApp.backend.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 역할 관리 컨트롤러 (관리자 전용)
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Tag(name = "Role", description = "역할 관리 API (관리자 전용)")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "모든 역할 조회", description = "시스템에 등록된 모든 역할을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "역할 상세 조회", description = "특정 역할의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<RoleResponse>> getRole(
            @Parameter(description = "역할 ID") @PathVariable Long roleId
    ) {
        RoleResponse role = roleService.getRole(roleId);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @PostMapping
    @Operation(summary = "역할 생성", description = "새로운 역할을 생성합니다.")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody RoleRequest request
    ) {
        RoleResponse role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("역할이 생성되었습니다", role));
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "역할 수정", description = "기존 역할의 정보를 수정합니다.")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @Parameter(description = "역할 ID") @PathVariable Long roleId,
            @Valid @RequestBody RoleRequest request
    ) {
        RoleResponse role = roleService.updateRole(roleId, request);
        return ResponseEntity.ok(ApiResponse.success("역할이 수정되었습니다", role));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "역할 삭제", description = "역할을 삭제합니다. 기본 역할(USER, ADMIN)은 삭제할 수 없습니다.")
    public ResponseEntity<ApiResponse<Void>> deleteRole(
            @Parameter(description = "역할 ID") @PathVariable Long roleId
    ) {
        roleService.deleteRole(roleId);
        return ResponseEntity.ok(ApiResponse.success("역할이 삭제되었습니다", null));
    }
}
