package com.TodoApp.backend.domain.permission.controller;

import com.TodoApp.backend.domain.permission.dto.RoleResponse;
import com.TodoApp.backend.domain.permission.dto.UserRoleRequest;
import com.TodoApp.backend.domain.permission.service.UserRoleService;
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
 * 사용자 역할 관리 컨트롤러 (관리자 전용)
 */
@RestController
@RequestMapping("/api/admin/users/{userId}/roles")
@RequiredArgsConstructor
@Tag(name = "UserRole", description = "사용자 역할 관리 API (관리자 전용)")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping
    @Operation(summary = "사용자 역할 조회", description = "특정 사용자에게 할당된 역할 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getUserRoles(
            @Parameter(description = "사용자 ID") @PathVariable Long userId
    ) {
        List<RoleResponse> roles = userRoleService.getUserRoles(userId);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @PostMapping
    @Operation(summary = "사용자에 역할 할당", description = "사용자에게 역할을 추가로 할당합니다.")
    public ResponseEntity<ApiResponse<Void>> assignRoleToUser(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "역할 ID") @RequestParam Long roleId
    ) {
        userRoleService.assignRoleToUser(userId, roleId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("역할이 할당되었습니다", null));
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "사용자에서 역할 제거", description = "사용자에게 할당된 역할을 제거합니다. 사용자는 최소 1개의 역할을 가져야 합니다.")
    public ResponseEntity<ApiResponse<Void>> removeRoleFromUser(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "역할 ID") @PathVariable Long roleId
    ) {
        userRoleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok(ApiResponse.success("역할이 제거되었습니다", null));
    }

    @PutMapping
    @Operation(summary = "사용자 역할 일괄 업데이트", description = "사용자의 역할을 일괄 업데이트합니다. 기존 역할은 모두 제거되고 새로운 역할로 교체됩니다.")
    public ResponseEntity<ApiResponse<Void>> updateUserRoles(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Valid @RequestBody UserRoleRequest request
    ) {
        userRoleService.updateUserRoles(userId, request);
        return ResponseEntity.ok(ApiResponse.success("사용자 역할이 업데이트되었습니다", null));
    }
}
