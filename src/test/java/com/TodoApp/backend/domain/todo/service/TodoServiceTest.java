package com.TodoApp.backend.domain.todo.service;

import com.TodoApp.backend.domain.todo.dto.TodoDashboardStatsResponse;
import com.TodoApp.backend.domain.todo.dto.TodoRequest;
import com.TodoApp.backend.domain.todo.dto.TodoResponse;
import com.TodoApp.backend.domain.todo.dto.TodoSearchRequest;
import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.domain.todo.event.TodoCreatedEvent;
import com.TodoApp.backend.domain.todo.event.TodoDeletedEvent;
import com.TodoApp.backend.domain.todo.event.TodoUpdatedEvent;
import com.TodoApp.backend.domain.todo.mapper.TodoMapper;
import com.TodoApp.backend.domain.todo.repository.TodoRepository;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.domain.user.repository.UserRepository;
import com.TodoApp.backend.fixture.TodoFixture;
import com.TodoApp.backend.fixture.UserFixture;
import com.TodoApp.backend.global.exception.BusinessException;
import com.core.test.utils.TestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;


@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService 테스트")
class TodoServiceTest {

    private static final TestSupport<TodoRequest> todoRequestSupport = new TestSupport<>(TodoRequest.class);
    private static final TestSupport<TodoSearchRequest> todoSearchRequestSupport = new TestSupport<>(TodoSearchRequest.class);
    private static final TestSupport<Todo> todoSupport = new TestSupport<>(Todo.class);
    
    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoService todoService;

    @Mock
    private TodoMapper todoMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private User testUser;
    private Todo testTodo;
    private TodoRequest todoRequest;

    @BeforeEach
    void setUp() {
        // Fixture를 사용하여 테스트 데이터 생성
        testUser = UserFixture.aUser();
        testTodo = TodoFixture.aTodoFor(testUser);

        todoRequest = todoRequestSupport.monkey();
        todoRequest.setTitle("새로운 TODO");
        todoRequest.setDescription("새로운 설명");
        todoRequest.setStatus(Todo.TodoStatus.TODO);
        todoRequest.setPriority(Todo.Priority.HIGH);

        // Mapper stub 추가
        lenient().when(todoMapper.toDto(any(Todo.class))).thenAnswer(invocation -> {
            Todo todo = invocation.getArgument(0);
            return TodoResponse.builder()
                    .id(todo.getId())
                    .userId(todo.getUser().getId())
                    .username(todo.getUser().getUsername())
                    .title(todo.getTitle())
                    .description(todo.getDescription())
                    .status(todo.getStatus() != null ? todo.getStatus().name() : null)
                    .priority(todo.getPriority() != null ? todo.getPriority().name() : null)
                    .dueDate(todo.getDueDate())
                    .completedAt(todo.getCompletedAt())
                    .position(todo.getPosition())
                    .projectId(todo.getProjectId())
                    .createdAt(todo.getCreatedAt())
                    .updatedAt(todo.getUpdatedAt())
                    .build();
        });

        lenient().when(todoMapper.toEntity(any(TodoRequest.class))).thenAnswer(invocation -> {
            TodoRequest request = invocation.getArgument(0);
            Todo todo = TodoFixture.aTodoFor(testUser);
            todo.setTitle(request.getTitle());
            todo.setDescription(request.getDescription());
            todo.setStatus(request.getStatus());
            todo.setPriority(request.getPriority());
            todo.setDueDate(request.getDueDate());
            todo.setPosition(request.getPosition());
            todo.setProjectId(request.getProjectId());
            return todo;
        });

        lenient().doAnswer(invocation -> {
            TodoRequest request = invocation.getArgument(0);
            Todo todo = invocation.getArgument(1);
            if (request.getTitle() != null) todo.setTitle(request.getTitle());
            if (request.getDescription() != null) todo.setDescription(request.getDescription());
            if (request.getStatus() != null) todo.setStatus(request.getStatus());
            if (request.getPriority() != null) todo.setPriority(request.getPriority());
            if (request.getDueDate() != null) todo.setDueDate(request.getDueDate());
            if (request.getPosition() != null) todo.setPosition(request.getPosition());
            if (request.getProjectId() != null) todo.setProjectId(request.getProjectId());
            return null;
        }).when(todoMapper).updateFromDto(any(TodoRequest.class), any(Todo.class));
    }

    @Test
    @DisplayName("TODO 생성 성공")
    void createTodo_성공() {
        // Given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> {
            Todo todo = invocation.getArgument(0);
            todo.setId(testTodo.getId());
            return todo;
        });

        // When
        var response = todoService.createTodo(testUser.getId(), todoRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("새로운 TODO"); // testTodo.getTitle() 대신 todoRequest의 제목 사용
        verify(userRepository).findById(testUser.getId());
        verify(todoRepository).save(any(Todo.class));
        
        // 이벤트 발행 검증
        ArgumentCaptor<TodoCreatedEvent> eventCaptor = ArgumentCaptor.forClass(TodoCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        TodoCreatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTodo()).isNotNull();
        assertThat(capturedEvent.getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("TODO 생성 실패 - 사용자 없음")
    void createTodo_실패_사용자_없음() {
        // Given
        Long nonExistentUserId = 999L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> todoService.createTodo(nonExistentUserId, todoRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");

        verify(userRepository).findById(nonExistentUserId);
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    @DisplayName("TODO 조회 성공")
    void getTodo_성공() {
        // Given
        when(todoRepository.findByIdAndUserId(testTodo.getId(), testUser.getId()))
                .thenReturn(Optional.of(testTodo));

        // When
        var response = todoService.getTodo(testUser.getId(), testTodo.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(testTodo.getTitle());
        verify(todoRepository).findByIdAndUserId(testTodo.getId(), testUser.getId());
    }

    @Test
    @DisplayName("TODO 조회 실패 - 권한 없음")
    void getTodo_실패_권한_없음() {
        // Given
        when(todoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> todoService.getTodo(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("TODO를 찾을 수 없습니다.");

        verify(todoRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("TODO 목록 조회 - 전체 조회")
    void getTodos_전체_조회() {
        // Given
        TodoSearchRequest searchRequest = todoSearchRequestSupport.monkey();
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setSortBy("createdAt");
        searchRequest.setSortDirection("DESC");

        // Fixture를 사용하여 여러 Todo 생성
        List<Todo> todos = TodoFixture.todosFor(testUser, 3);
        Page<Todo> todoPage = new PageImpl<>(todos);
        
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(todoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(todoPage);

        // When
        Page<com.TodoApp.backend.domain.todo.dto.TodoResponse> response = todoService.getTodos(testUser.getId(), searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(3);
        verify(userRepository).findById(testUser.getId());
        verify(todoRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TODO 목록 조회 - 키워드 검색")
    void getTodos_키워드_검색() {
        // Given
        TodoSearchRequest searchRequest = todoSearchRequestSupport.monkey();
        searchRequest.setKeyword("테스트");
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setSortBy("createdAt");
        searchRequest.setSortDirection("DESC");

        Page<Todo> todoPage = new PageImpl<>(Arrays.asList(testTodo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(todoPage);

        // When
        Page<com.TodoApp.backend.domain.todo.dto.TodoResponse> response = todoService.getTodos(1L, searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(userRepository).findById(1L);
        verify(todoRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TODO 목록 조회 - 상태 필터")
    void getTodos_상태_필터() {
        // Given
        TodoSearchRequest searchRequest = todoSearchRequestSupport.monkey();
        searchRequest.setStatus(Todo.TodoStatus.TODO);
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setSortBy("createdAt");
        searchRequest.setSortDirection("DESC");

        Page<Todo> todoPage = new PageImpl<>(Arrays.asList(testTodo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(todoPage);

        // When
        Page<com.TodoApp.backend.domain.todo.dto.TodoResponse> response = todoService.getTodos(1L, searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(userRepository).findById(1L);
        verify(todoRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TODO 목록 조회 - 우선순위 필터")
    void getTodos_우선순위_필터() {
        // Given
        TodoSearchRequest searchRequest = todoSearchRequestSupport.monkey();
        searchRequest.setPriority(Todo.Priority.HIGH);
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setSortBy("createdAt");
        searchRequest.setSortDirection("DESC");

        Page<Todo> todoPage = new PageImpl<>(Arrays.asList(testTodo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(todoPage);

        // When
        Page<com.TodoApp.backend.domain.todo.dto.TodoResponse> response = todoService.getTodos(1L, searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(userRepository).findById(1L);
        verify(todoRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TODO 목록 조회 - 마감일 범위 필터")
    void getTodos_마감일_범위_필터() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 23, 59);

        TodoSearchRequest searchRequest = todoSearchRequestSupport.monkey();
        searchRequest.setDueDateStart(startDate);
        searchRequest.setDueDateEnd(endDate);
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setSortBy("createdAt");
        searchRequest.setSortDirection("DESC");

        Page<Todo> todoPage = new PageImpl<>(Arrays.asList(testTodo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(todoPage);

        // When
        Page<com.TodoApp.backend.domain.todo.dto.TodoResponse> response = todoService.getTodos(1L, searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(userRepository).findById(1L);
        verify(todoRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TODO 목록 조회 - 프로젝트 필터")
    void getTodos_프로젝트_필터() {
        // Given
        TodoSearchRequest searchRequest = todoSearchRequestSupport.monkey();
        searchRequest.setProjectId(1L);
        searchRequest.setPage(0);
        searchRequest.setSize(10);
        searchRequest.setSortBy("createdAt");
        searchRequest.setSortDirection("DESC");

        Page<Todo> todoPage = new PageImpl<>(Arrays.asList(testTodo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(todoPage);

        // When
        Page<com.TodoApp.backend.domain.todo.dto.TodoResponse> response = todoService.getTodos(1L, searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(userRepository).findById(1L);
        verify(todoRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("TODO 수정 성공")
    void updateTodo_성공() {
        // Given
        TodoRequest updateRequest = todoRequestSupport.monkey();
        updateRequest.setTitle("수정된 제목");
        updateRequest.setDescription("수정된 설명");
        updateRequest.setStatus(Todo.TodoStatus.IN_PROGRESS);
        updateRequest.setPriority(Todo.Priority.HIGH);

        Todo updatedTodo = TodoFixture.aTodoFor(testUser);
        updatedTodo.setTitle("수정된 제목");
        updatedTodo.setDescription("수정된 설명");
        updatedTodo.setStatus(Todo.TodoStatus.IN_PROGRESS);
        updatedTodo.setPriority(Todo.Priority.HIGH);
        updatedTodo.setId(1L);

        when(todoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);

        // When
        var response = todoService.updateTodo(1L, 1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
        assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
        verify(todoRepository).findByIdAndUserId(1L, 1L);
        verify(todoRepository).save(any(Todo.class));
        
        // 이벤트 발행 검증
        ArgumentCaptor<TodoUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(TodoUpdatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        TodoUpdatedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTodo()).isNotNull();
        assertThat(capturedEvent.getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("TODO 수정 실패 - 권한 없음")
    void updateTodo_실패_권한_없음() {
        // Given
        TodoRequest updateRequest = todoRequestSupport.monkey();
        updateRequest.setTitle("수정된 제목");

        when(todoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> todoService.updateTodo(1L, 1L, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("TODO를 찾을 수 없습니다.");

        verify(todoRepository).findByIdAndUserId(1L, 1L);
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    @DisplayName("TODO 상태 변경 성공")
    void updateTodoStatus_성공() {
        // Given
        Todo updatedTodo = TodoFixture.aCompletedTodoFor(testUser);
        updatedTodo.setTitle("테스트 TODO");
        updatedTodo.setId(1L);

        when(todoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTodo));
        when(todoRepository.save(any(Todo.class))).thenReturn(updatedTodo);

        // When
        var response = todoService.updateTodoStatus(1L, 1L, Todo.TodoStatus.DONE);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("DONE");
        verify(todoRepository).findByIdAndUserId(1L, 1L);
        verify(todoRepository).save(any(Todo.class));
    }

    @Test
    @DisplayName("TODO 삭제 성공")
    void deleteTodo_성공() {
        // Given
        when(todoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testTodo));
        doNothing().when(todoRepository).delete(any(Todo.class));

        // When
        todoService.deleteTodo(1L, 1L);

        // Then
        verify(todoRepository).findByIdAndUserId(1L, 1L);
        verify(todoRepository).delete(testTodo);
        
        // 이벤트 발행 검증
        ArgumentCaptor<TodoDeletedEvent> eventCaptor = ArgumentCaptor.forClass(TodoDeletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        TodoDeletedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getTodo()).isEqualTo(testTodo);
        assertThat(capturedEvent.getUser()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("사용자 통계 조회")
    void getUserStats_통계_조회() {
        // Given
        List<Todo> overdueTodos = Arrays.asList(testTodo);
        when(todoRepository.countByUserId(1L)).thenReturn(10L);
        when(todoRepository.countByUserIdAndStatus(1L, Todo.TodoStatus.TODO)).thenReturn(5L);
        when(todoRepository.countByUserIdAndStatus(1L, Todo.TodoStatus.IN_PROGRESS)).thenReturn(3L);
        when(todoRepository.countByUserIdAndStatus(1L, Todo.TodoStatus.DONE)).thenReturn(2L);
        when(todoRepository.findOverdueTodos(eq(1L), any(LocalDateTime.class))).thenReturn(overdueTodos);

        // When
        var stats = todoService.getUserStats(1L);

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getTotalCount()).isEqualTo(10L);
        assertThat(stats.getTodoCount()).isEqualTo(5L);
        assertThat(stats.getInProgressCount()).isEqualTo(3L);
        assertThat(stats.getDoneCount()).isEqualTo(2L);
        assertThat(stats.getOverdueCount()).isEqualTo(1L);
        assertThat(stats.getCompletionRate()).isEqualTo(20.0);

        verify(todoRepository).countByUserId(1L);
        verify(todoRepository).countByUserIdAndStatus(1L, Todo.TodoStatus.TODO);
        verify(todoRepository).countByUserIdAndStatus(1L, Todo.TodoStatus.IN_PROGRESS);
        verify(todoRepository).countByUserIdAndStatus(1L, Todo.TodoStatus.DONE);
        verify(todoRepository).findOverdueTodos(eq(1L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("대시보드 통계 조회")
    void getDashboardStats_통계_조회() {
        // Given
        Long userId = 1L;
        long totalCount = 10L;
        long todoCount = 5L;
        long inProgressCount = 3L;
        long doneCount = 2L;
        List<Todo> overdueTodos = Arrays.asList(testTodo);
        double expectedCompletionRate = 20.0; // (2 / 10) * 100

        // 기본 통계 Mock 설정
        when(todoRepository.countByUserId(userId)).thenReturn(totalCount);
        when(todoRepository.countByUserIdAndStatus(userId, Todo.TodoStatus.TODO)).thenReturn(todoCount);
        when(todoRepository.countByUserIdAndStatus(userId, Todo.TodoStatus.IN_PROGRESS)).thenReturn(inProgressCount);
        when(todoRepository.countByUserIdAndStatus(userId, Todo.TodoStatus.DONE)).thenReturn(doneCount);
        when(todoRepository.findOverdueTodos(eq(userId), any(LocalDateTime.class))).thenReturn(overdueTodos);

        // 상태별 통계 Mock 설정
        List<TodoDashboardStatsResponse.StatusStats> statusStats = Arrays.asList(
                TodoDashboardStatsResponse.StatusStats.builder()
                        .status("TODO")
                        .count(5L)
                        .percentage(50.0)
                        .build(),
                TodoDashboardStatsResponse.StatusStats.builder()
                        .status("IN_PROGRESS")
                        .count(3L)
                        .percentage(30.0)
                        .build(),
                TodoDashboardStatsResponse.StatusStats.builder()
                        .status("DONE")
                        .count(2L)
                        .percentage(20.0)
                        .build()
        );
        when(todoRepository.findStatusStatsByUserId(userId, totalCount)).thenReturn(statusStats);

        // 우선순위별 통계 Mock 설정
        List<TodoDashboardStatsResponse.PriorityStats> priorityStats = Arrays.asList(
                TodoDashboardStatsResponse.PriorityStats.builder()
                        .priority("HIGH")
                        .count(4L)
                        .percentage(40.0)
                        .build(),
                TodoDashboardStatsResponse.PriorityStats.builder()
                        .priority("MEDIUM")
                        .count(4L)
                        .percentage(40.0)
                        .build(),
                TodoDashboardStatsResponse.PriorityStats.builder()
                        .priority("LOW")
                        .count(2L)
                        .percentage(20.0)
                        .build()
        );
        when(todoRepository.findPriorityStatsByUserId(userId, totalCount)).thenReturn(priorityStats);

        // 프로젝트별 통계 Mock 설정
        List<TodoDashboardStatsResponse.ProjectStats> projectStats = Arrays.asList(
                TodoDashboardStatsResponse.ProjectStats.builder()
                        .projectId(1L)
                        .projectName("프로젝트 1")
                        .projectColor("#FF5733")
                        .todoCount(6L)
                        .percentage(60.0)
                        .build(),
                TodoDashboardStatsResponse.ProjectStats.builder()
                        .projectId(null)
                        .projectName("프로젝트 없음")
                        .projectColor("#9CA3AF")
                        .todoCount(4L)
                        .percentage(40.0)
                        .build()
        );
        when(todoRepository.findProjectStatsByUserId(userId, totalCount)).thenReturn(projectStats);

        // When
        var stats = todoService.getDashboardStats(userId);

        // Then
        assertThat(stats).isNotNull();
        
        // 기본 통계 검증
        assertThat(stats.getBasicStats()).isNotNull();
        assertThat(stats.getBasicStats().getTotalCount()).isEqualTo(totalCount);
        assertThat(stats.getBasicStats().getTodoCount()).isEqualTo(todoCount);
        assertThat(stats.getBasicStats().getInProgressCount()).isEqualTo(inProgressCount);
        assertThat(stats.getBasicStats().getDoneCount()).isEqualTo(doneCount);
        assertThat(stats.getBasicStats().getOverdueCount()).isEqualTo(1L);
        assertThat(stats.getBasicStats().getCompletionRate()).isEqualTo(expectedCompletionRate);
        
        // 상태별 통계 검증
        assertThat(stats.getStatusStats()).isNotNull();
        assertThat(stats.getStatusStats()).hasSize(3);
        assertThat(stats.getStatusStats().get(0).getStatus()).isEqualTo("TODO");
        assertThat(stats.getStatusStats().get(0).getCount()).isEqualTo(5L);
        
        // 우선순위별 통계 검증
        assertThat(stats.getPriorityStats()).isNotNull();
        assertThat(stats.getPriorityStats()).hasSize(3);
        assertThat(stats.getPriorityStats().get(0).getPriority()).isEqualTo("HIGH");
        assertThat(stats.getPriorityStats().get(0).getCount()).isEqualTo(4L);
        
        // 프로젝트별 통계 검증
        assertThat(stats.getProjectStats()).isNotNull();
        assertThat(stats.getProjectStats()).hasSize(2);
        assertThat(stats.getProjectStats().get(0).getProjectId()).isEqualTo(1L);
        assertThat(stats.getProjectStats().get(0).getProjectName()).isEqualTo("프로젝트 1");

        // Repository 메서드 호출 검증
        verify(todoRepository).countByUserId(userId);
        verify(todoRepository).countByUserIdAndStatus(userId, Todo.TodoStatus.TODO);
        verify(todoRepository).countByUserIdAndStatus(userId, Todo.TodoStatus.IN_PROGRESS);
        verify(todoRepository).countByUserIdAndStatus(userId, Todo.TodoStatus.DONE);
        verify(todoRepository).findOverdueTodos(eq(userId), any(LocalDateTime.class));
        verify(todoRepository).findStatusStatsByUserId(userId, totalCount);
        verify(todoRepository).findPriorityStatsByUserId(userId, totalCount);
        verify(todoRepository).findProjectStatsByUserId(userId, totalCount);
    }
}

