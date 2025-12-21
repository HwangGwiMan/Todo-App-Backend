package com.TodoApp.backend.domain.todo.service;

import com.TodoApp.backend.domain.todo.dto.TodoDashboardStatsResponse;
import com.TodoApp.backend.domain.todo.dto.TodoRequest;
import com.TodoApp.backend.domain.todo.dto.TodoResponse;
import com.TodoApp.backend.domain.todo.dto.TodoSearchRequest;
import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.domain.todo.repository.TodoRepository;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.domain.user.repository.UserRepository;
import com.TodoApp.backend.global.exception.BusinessException;
import com.TodoApp.backend.global.exception.ErrorCode;
import com.TodoApp.backend.domain.todo.repository.specification.TodoSpecification;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    // DSLContext 제거 가능 (Repository에서 처리)

    /**
     * TODO 생성
     */
    @Transactional
    public TodoResponse createTodo(Long userId, TodoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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
                .orElseThrow(() -> new BusinessException(ErrorCode.TODO_NOT_FOUND));

        return TodoResponse.from(todo);
    }

    public Page<TodoResponse> getTodos(@NonNull Long userId, TodoSearchRequest searchRequest) {
        // 사용자 존재 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Specification 조합
        Specification<Todo> spec = Specification
                .where(TodoSpecification.hasUserId(userId))
                .and(TodoSpecification.hasKeyword(searchRequest.getKeyword()))
                .and(TodoSpecification.hasStatus(searchRequest.getStatus()))
                .and(TodoSpecification.hasPriority(searchRequest.getPriority()))
                .and(TodoSpecification.hasProjectId(searchRequest.getProjectId()))
                .and(TodoSpecification.dueDateBetween(
                        searchRequest.getDueDateStart(),
                        searchRequest.getDueDateEnd()
                ));

        // Pageable 생성 (정렬 포함)
        Pageable pageable = createPageable(searchRequest);

        // Specification을 사용한 동적 쿼리 실행
        Page<Todo> todos = todoRepository.findAll(spec, pageable);

        return todos.map(TodoResponse::from);
    }

    /**
     * TODO 수정
     */
    @Transactional
    public TodoResponse updateTodo(Long userId, Long todoId, TodoRequest request) {
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TODO_NOT_FOUND));

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
                .orElseThrow(() -> new BusinessException(ErrorCode.TODO_NOT_FOUND));

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
                .orElseThrow(() -> new BusinessException(ErrorCode.TODO_NOT_FOUND));

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
     * 대시보드 통계 조회 (JOOQ 사용)
     */
    public TodoDashboardStatsResponse getDashboardStats(Long userId) {
        // 기본 통계
        long totalCount = todoRepository.countByUserId(userId);
        long todoCount = todoRepository.countByUserIdAndStatus(userId, Todo.TodoStatus.TODO);
        long inProgressCount = todoRepository.countByUserIdAndStatus(userId, Todo.TodoStatus.IN_PROGRESS);
        long doneCount = todoRepository.countByUserIdAndStatus(userId, Todo.TodoStatus.DONE);
        List<Todo> overdueTodos = todoRepository.findOverdueTodos(userId, LocalDateTime.now());
        double completionRate = totalCount > 0 ? (double) doneCount / totalCount * 100 : 0.0;

        TodoDashboardStatsResponse.BasicStats basicStats = TodoDashboardStatsResponse.BasicStats.builder()
                .totalCount(totalCount)
                .todoCount(todoCount)
                .inProgressCount(inProgressCount)
                .doneCount(doneCount)
                .overdueCount((long) overdueTodos.size())
                .completionRate(completionRate)
                .build();

        // 상태별 통계 (JOOQ) - Repository로 위임
        List<TodoDashboardStatsResponse.StatusStats> statusStats = 
                todoRepository.findStatusStatsByUserId(userId, totalCount);

        // 우선순위별 통계 (JOOQ) - Repository로 위임
        List<TodoDashboardStatsResponse.PriorityStats> priorityStats = 
                todoRepository.findPriorityStatsByUserId(userId, totalCount);

        // 프로젝트별 통계 (JOOQ) - Repository로 위임
        List<TodoDashboardStatsResponse.ProjectStats> projectStats = 
                todoRepository.findProjectStatsByUserId(userId, totalCount);

        return TodoDashboardStatsResponse.builder()
                .basicStats(basicStats)
                .statusStats(statusStats)
                .priorityStats(priorityStats)
                .projectStats(projectStats)
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

