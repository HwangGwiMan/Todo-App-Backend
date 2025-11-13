package com.TodoApp.backend.domain.todo.controller;

import com.TodoApp.backend.domain.todo.dto.TodoRequest;
import com.TodoApp.backend.domain.todo.dto.TodoResponse;
import com.TodoApp.backend.domain.todo.dto.TodoSearchRequest;
import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.domain.todo.service.TodoService;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.global.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@Tag(name = "Todo", description = "TODO 관리 API")
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    @Operation(summary = "TODO 생성", description = "새로운 TODO를 생성합니다.")
    public ResponseEntity<ApiResponse<TodoResponse>> createTodo(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TodoRequest request
    ) {
        TodoResponse response = todoService.createTodo(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("TODO가 생성되었습니다", response));
    }

    @GetMapping("/{todoId}")
    @Operation(summary = "TODO 상세 조회", description = "특정 TODO의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<TodoResponse>> getTodo(
            @AuthenticationPrincipal User user,
            @Parameter(description = "TODO ID") @PathVariable Long todoId
    ) {
        TodoResponse response = todoService.getTodo(user.getId(), todoId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "TODO 목록 조회", description = "TODO 목록을 조회합니다. 검색, 필터링, 정렬, 페이징을 지원합니다.")
    public ResponseEntity<ApiResponse<Page<TodoResponse>>> getTodos(
            @AuthenticationPrincipal User user,
            @Parameter(description = "검색 조건") @ModelAttribute TodoSearchRequest searchRequest
    ) {
        Page<TodoResponse> response = todoService.getTodos(user.getId(), searchRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{todoId}")
    @Operation(summary = "TODO 수정", description = "기존 TODO를 수정합니다.")
    public ResponseEntity<ApiResponse<TodoResponse>> updateTodo(
            @AuthenticationPrincipal User user,
            @Parameter(description = "TODO ID") @PathVariable Long todoId,
            @Valid @RequestBody TodoRequest request
    ) {
        TodoResponse response = todoService.updateTodo(user.getId(), todoId, request);
        return ResponseEntity.ok(ApiResponse.success("TODO가 수정되었습니다", response));
    }

    @PatchMapping("/{todoId}/status")
    @Operation(summary = "TODO 상태 변경", description = "TODO의 상태를 변경합니다.")
    public ResponseEntity<ApiResponse<TodoResponse>> updateTodoStatus(
            @AuthenticationPrincipal User user,
            @Parameter(description = "TODO ID") @PathVariable Long todoId,
            @Parameter(description = "변경할 상태") @RequestParam Todo.TodoStatus status
    ) {
        TodoResponse response = todoService.updateTodoStatus(user.getId(), todoId, status);
        return ResponseEntity.ok(ApiResponse.success("TODO 상태가 변경되었습니다", response));
    }

    @DeleteMapping("/{todoId}")
    @Operation(summary = "TODO 삭제", description = "TODO를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteTodo(
            @AuthenticationPrincipal User user,
            @Parameter(description = "TODO ID") @PathVariable Long todoId
    ) {
        todoService.deleteTodo(user.getId(), todoId);
        return ResponseEntity.ok(ApiResponse.success("TODO가 삭제되었습니다", null));
    }

    @GetMapping("/stats")
    @Operation(summary = "사용자 통계 조회", description = "사용자의 TODO 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<TodoService.TodoStatsResponse>> getUserStats(
            @AuthenticationPrincipal User user
    ) {
        TodoService.TodoStatsResponse response = todoService.getUserStats(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

