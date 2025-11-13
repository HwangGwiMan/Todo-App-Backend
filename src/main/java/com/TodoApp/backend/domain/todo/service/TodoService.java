package com.TodoApp.backend.domain.todo.service;

import com.TodoApp.backend.domain.todo.dto.TodoRequest;
import com.TodoApp.backend.domain.todo.dto.TodoResponse;
import com.TodoApp.backend.domain.todo.dto.TodoSearchRequest;
import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.domain.todo.repository.TodoRepository;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    /**
     * TODO 생성
     */
    @Transactional
    public TodoResponse createTodo(Long userId, TodoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Todo todo = Todo.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Todo.TodoStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : Todo.Priority.MEDIUM)
                .dueDate(request.getDueDate())
                .position(request.getPosition() != null ? request.getPosition() : 0)
                .projectId(request.getProjectId())
                .build();

        Todo savedTodo = todoRepository.save(todo);
        log.info("TODO 생성 완료: userId={}, todoId={}", userId, savedTodo.getId());

        return TodoResponse.from(savedTodo);
    }

    /**
     * TODO 조회 (단건) - 권한 체크
     */
    public TodoResponse getTodo(Long userId, Long todoId) {
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new RuntimeException("TODO를 찾을 수 없거나 권한이 없습니다."));

        return TodoResponse.from(todo);
    }

    /**
     * TODO 목록 조회 (검색, 필터링, 정렬, 페이징)
     */
    public Page<TodoResponse> getTodos(Long userId, TodoSearchRequest searchRequest) {
        Pageable pageable = createPageable(searchRequest);

        Page<Todo> todos;

        // 키워드 검색
        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().isEmpty()) {
            todos = todoRepository.searchByKeyword(userId, searchRequest.getKeyword(), pageable);
        }
        // 상태 필터
        else if (searchRequest.getStatus() != null) {
            todos = todoRepository.findByUserIdAndStatus(userId, searchRequest.getStatus(), pageable);
        }
        // 우선순위 필터
        else if (searchRequest.getPriority() != null) {
            todos = todoRepository.findByUserIdAndPriority(userId, searchRequest.getPriority(), pageable);
        }
        // 마감일 범위 필터
        else if (searchRequest.getDueDateStart() != null && searchRequest.getDueDateEnd() != null) {
            todos = todoRepository.findByUserIdAndDueDateBetween(
                    userId,
                    searchRequest.getDueDateStart(),
                    searchRequest.getDueDateEnd(),
                    pageable
            );
        }
        // 전체 조회
        else {
            todos = todoRepository.findByUserId(userId, pageable);
        }

        return todos.map(TodoResponse::from);
    }

    /**
     * TODO 수정
     */
    @Transactional
    public TodoResponse updateTodo(Long userId, Long todoId, TodoRequest request) {
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new RuntimeException("TODO를 찾을 수 없거나 권한이 없습니다."));

        // 수정 가능한 필드 업데이트
        if (request.getTitle() != null) {
            todo.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            todo.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            todo.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            todo.setPriority(request.getPriority());
        }
        if (request.getDueDate() != null) {
            todo.setDueDate(request.getDueDate());
        }
        if (request.getPosition() != null) {
            todo.setPosition(request.getPosition());
        }
        if (request.getProjectId() != null) {
            todo.setProjectId(request.getProjectId());
        }

        Todo updatedTodo = todoRepository.save(todo);
        log.info("TODO 수정 완료: userId={}, todoId={}", userId, todoId);

        return TodoResponse.from(updatedTodo);
    }

    /**
     * TODO 상태 변경
     */
    @Transactional
    public TodoResponse updateTodoStatus(Long userId, Long todoId, Todo.TodoStatus status) {
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new RuntimeException("TODO를 찾을 수 없거나 권한이 없습니다."));

        todo.setStatus(status);
        Todo updatedTodo = todoRepository.save(todo);
        log.info("TODO 상태 변경: userId={}, todoId={}, status={}", userId, todoId, status);

        return TodoResponse.from(updatedTodo);
    }

    /**
     * TODO 삭제
     */
    @Transactional
    public void deleteTodo(Long userId, Long todoId) {
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new RuntimeException("TODO를 찾을 수 없거나 권한이 없습니다."));

        todoRepository.delete(todo);
        log.info("TODO 삭제 완료: userId={}, todoId={}", userId, todoId);
    }

    /**
     * 사용자 통계 조회
     */
    public TodoStatsResponse getUserStats(Long userId) {
        long totalCount = todoRepository.countByUserId(userId);
        long todoCount = todoRepository.countByUserIdAndStatus(userId, Todo.TodoStatus.TODO);
        long inProgressCount = todoRepository.countByUserIdAndStatus(userId, Todo.TodoStatus.IN_PROGRESS);
        long doneCount = todoRepository.countByUserIdAndStatus(userId, Todo.TodoStatus.DONE);

        List<Todo> overdueTodos = todoRepository.findOverdueTodos(userId, LocalDateTime.now());

        return TodoStatsResponse.builder()
                .totalCount(totalCount)
                .todoCount(todoCount)
                .inProgressCount(inProgressCount)
                .doneCount(doneCount)
                .overdueCount((long) overdueTodos.size())
                .completionRate(totalCount > 0 ? (double) doneCount / totalCount * 100 : 0.0)
                .build();
    }

    /**
     * Pageable 생성 헬퍼 메서드
     */
    private Pageable createPageable(TodoSearchRequest searchRequest) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(searchRequest.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sort = Sort.by(direction, searchRequest.getSortBy());

        return PageRequest.of(
                searchRequest.getPage(),
                searchRequest.getSize(),
                sort
        );
    }

    /**
     * 통계 응답 DTO (내부 클래스)
     */
    @lombok.Data
    @lombok.Builder
    public static class TodoStatsResponse {
        private Long totalCount;
        private Long todoCount;
        private Long inProgressCount;
        private Long doneCount;
        private Long overdueCount;
        private Double completionRate;
    }
}

