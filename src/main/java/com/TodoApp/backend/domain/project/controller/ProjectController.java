package com.TodoApp.backend.domain.project.controller;

import com.TodoApp.backend.domain.project.dto.ProjectRequest;
import com.TodoApp.backend.domain.project.dto.ProjectResponse;
import com.TodoApp.backend.domain.project.service.ProjectService;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 프로젝트 API 컨트롤러
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "프로젝트 관리 API")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(
            summary = "프로젝트 목록 조회",
            description = "현재 로그인한 사용자의 모든 프로젝트를 정렬 순서대로 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로젝트 목록 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        List<ProjectResponse> projects = projectService.getProjectsByUser(user);
        return ResponseEntity.ok(ApiResponse.success("프로젝트 목록을 조회했습니다.", projects));
    }

    @Operation(
            summary = "프로젝트 상세 조회",
            description = "특정 프로젝트의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로젝트 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        try {
            ProjectResponse project = projectService.getProject(projectId, user);
            return ResponseEntity.ok(ApiResponse.success("프로젝트를 조회했습니다.", project));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(
            summary = "프로젝트 생성",
            description = "새로운 프로젝트를 생성합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "프로젝트 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "프로젝트명 중복")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Parameter(description = "프로젝트 생성 요청 데이터") @Valid @RequestBody ProjectRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        try {
            ProjectResponse createdProject = projectService.createProject(request, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("프로젝트가 생성되었습니다.", createdProject));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @Operation(
            summary = "프로젝트 수정",
            description = "기존 프로젝트의 정보를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로젝트 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "프로젝트명 중복")
    })
    @PutMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @Parameter(description = "프로젝트 수정 요청 데이터") @Valid @RequestBody ProjectRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        try {
            ProjectResponse updatedProject = projectService.updateProject(projectId, request, user);
            return ResponseEntity.ok(ApiResponse.success("프로젝트가 수정되었습니다.", updatedProject));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.error(e.getMessage()));
            }
        }
    }

    @Operation(
            summary = "프로젝트 삭제",
            description = "기존 프로젝트를 삭제합니다. 기본 프로젝트는 삭제할 수 없습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로젝트 삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "기본 프로젝트는 삭제 불가"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트를 찾을 수 없음")
    })
    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        try {
            projectService.deleteProject(projectId, user);
            return ResponseEntity.ok(ApiResponse.success("프로젝트가 삭제되었습니다.", null));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(e.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error(e.getMessage()));
            }
        }
    }

    @Operation(
            summary = "기본 프로젝트 조회",
            description = "현재 사용자의 기본 프로젝트를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "기본 프로젝트 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기본 프로젝트가 없음")
    })
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<ProjectResponse>> getDefaultProject(
            @Parameter(hidden = true) @AuthenticationPrincipal User user
    ) {
        ProjectResponse defaultProject = projectService.getDefaultProject(user);
        if (defaultProject == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("기본 프로젝트가 없습니다."));
        }
        return ResponseEntity.ok(ApiResponse.success("기본 프로젝트를 조회했습니다.", defaultProject));
    }
}
