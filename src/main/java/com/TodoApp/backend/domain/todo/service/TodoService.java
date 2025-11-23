package com.TodoApp.backend.domain.todo.service;

import com.TodoApp.backend.domain.todo.dto.TodoDashboardStatsResponse;
import com.TodoApp.backend.domain.todo.dto.TodoRequest;
import com.TodoApp.backend.domain.todo.dto.TodoResponse;
import com.TodoApp.backend.domain.todo.dto.TodoSearchRequest;
import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.domain.todo.repository.TodoRepository;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final DSLContext dslContext;

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Page<Todo> todos;

        // 프로젝트 필터 처리
        if (searchRequest.getProjectId() != null) {
            // 프로젝트별 TODO 조회
            todos = todoRepository.findByUserAndProjectId(user, searchRequest.getProjectId(), pageable);
        } else {
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

        // 상태별 통계 (JOOQ)
        List<TodoDashboardStatsResponse.StatusStats> statusStats = getStatusStatsByJooq(userId, totalCount);

        // 우선순위별 통계 (JOOQ)
        List<TodoDashboardStatsResponse.PriorityStats> priorityStats = getPriorityStatsByJooq(userId, totalCount);

        // 프로젝트별 통계 (JOOQ)
        List<TodoDashboardStatsResponse.ProjectStats> projectStats = getProjectStatsByJooq(userId, totalCount);

        return TodoDashboardStatsResponse.builder()
                .basicStats(basicStats)
                .statusStats(statusStats)
                .priorityStats(priorityStats)
                .projectStats(projectStats)
                .build();
    }

    /**
     * 상태별 통계 조회 (JOOQ)
     */
    private List<TodoDashboardStatsResponse.StatusStats> getStatusStatsByJooq(Long userId, long totalCount) {
        var result = dslContext
                .select(
                        DSL.field("status", String.class).as("status"),
                        DSL.count().as("count")
                )
                .from(DSL.table("todos"))
                .where(DSL.field("user_id").eq(userId))
                .groupBy(DSL.field("status"))
                .fetch();

        return result.stream()
                .map(record -> {
                    String status = record.get("status", String.class);
                    Long count = record.get("count", Long.class);
                    long countValue = count != null ? count : 0L;
                    double percentage = totalCount > 0 ? (double) countValue / totalCount * 100 : 0.0;

                    return TodoDashboardStatsResponse.StatusStats.builder()
                            .status(status != null ? status : "")
                            .count(countValue)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 우선순위별 통계 조회 (JOOQ)
     */
    private List<TodoDashboardStatsResponse.PriorityStats> getPriorityStatsByJooq(Long userId, long totalCount) {
        var result = dslContext
                .select(
                        DSL.field("priority", String.class).as("priority"),
                        DSL.count().as("count")
                )
                .from(DSL.table("todos"))
                .where(DSL.field("user_id").eq(userId))
                .and(DSL.field("priority").isNotNull())
                .groupBy(DSL.field("priority"))
                .fetch();

        return result.stream()
                .map(record -> {
                    String priority = record.get("priority", String.class);
                    Long count = record.get("count", Long.class);
                    long countValue = count != null ? count : 0L;
                    double percentage = totalCount > 0 ? (double) countValue / totalCount * 100 : 0.0;

                    return TodoDashboardStatsResponse.PriorityStats.builder()
                            .priority(priority != null ? priority : "")
                            .count(countValue)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 프로젝트별 통계 조회 (JOOQ)
     */
    private List<TodoDashboardStatsResponse.ProjectStats> getProjectStatsByJooq(Long userId, long totalCount) {

        // 프로젝트별 통계 (프로젝트가 있는 TODO)
        var result = dslContext
                .select(
                        DSL.field("t.project_id").as("project_id"),
                        DSL.field("p.name").as("project_name"),
                        DSL.field("p.color").as("project_color"),
                        DSL.count().as("todo_count")
                )
                .from(DSL.table("todos").as("t"))
                .leftJoin(DSL.table("projects").as("p"))
                .on(DSL.field("t.project_id").eq(DSL.field("p.id")))
                .where(DSL.field("t.user_id").eq(userId))
                .and(DSL.field("t.project_id").isNotNull())
                .groupBy(
                        DSL.field("t.project_id"),
                        DSL.field("p.name"),
                        DSL.field("p.color")
                )
                .orderBy(DSL.count().desc())
                .fetch();

        List<TodoDashboardStatsResponse.ProjectStats> projectStats = result.stream()
                .map(record -> {
                    Long projectId = record.get("project_id", Long.class);
                    String projectName = record.get("project_name", String.class);
                    String projectColor = record.get("project_color", String.class);
                    Long todoCount = record.get("todo_count", Long.class);
                    long todoCountValue = todoCount != null ? todoCount : 0L;
                    double percentage = totalCount > 0 ? (double) todoCountValue / totalCount * 100 : 0.0;

                    return TodoDashboardStatsResponse.ProjectStats.builder()
                            .projectId(projectId)
                            .projectName(projectName != null ? projectName : "알 수 없음")
                            .projectColor(projectColor != null ? projectColor : "#9CA3AF")
                            .todoCount(todoCountValue)
                            .percentage(percentage)
                            .build();
                }).collect(Collectors.toList());

        // 프로젝트가 없는 TODO 통계
        Long noProjectCount = dslContext
                .selectCount()
                .from(DSL.table("todos"))
                .where(DSL.field("user_id").eq(userId))
                .and(DSL.field("project_id").isNull())
                .fetchOne(0, Long.class);

        if (noProjectCount != null && noProjectCount > 0) {
            double percentage = totalCount > 0 ? (double) noProjectCount / totalCount * 100 : 0.0;
            projectStats.add(TodoDashboardStatsResponse.ProjectStats.builder()
                    .projectId(null)
                    .projectName("프로젝트 없음")
                    .projectColor("#9CA3AF")
                    .todoCount(noProjectCount)
                    .percentage(percentage)
                    .build());
        }

        return projectStats;
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

