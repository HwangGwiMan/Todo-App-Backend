package com.TodoApp.backend.domain.todo.service;

import com.TodoApp.backend.domain.todo.dto.TodoRequest;
import com.TodoApp.backend.domain.todo.dto.TodoSearchRequest;
import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.domain.todo.repository.TodoRepository;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.domain.user.repository.UserRepository;
import com.TodoApp.backend.fixture.TodoFixture;
import com.TodoApp.backend.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService 테스트")
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TodoService todoService;

    private User testUser;
    private Todo testTodo;
    private TodoRequest todoRequest;

    @BeforeEach
    void setUp() {
        // Fixture를 사용하여 테스트 데이터 생성
        testUser = UserFixture.aUser();
        testTodo = TodoFixture.aTodoFor(testUser);

        todoRequest = TodoRequest.builder()
                .title("새로운 TODO")
                .description("새로운 설명")
                .status(Todo.TodoStatus.TODO)
                .priority(Todo.Priority.HIGH)
                .build();
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
        assertThat(response.getTitle()).isEqualTo(testTodo.getTitle());
        verify(userRepository).findById(testUser.getId());
        verify(todoRepository).save(any(Todo.class));
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
                .isInstanceOf(RuntimeException.class)
                .hasMessage("TODO를 찾을 수 없거나 권한이 없습니다.");

        verify(todoRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("TODO 목록 조회 - 전체 조회")
    void getTodos_전체_조회() {
        // Given
        TodoSearchRequest searchRequest = TodoSearchRequest.builder()
                .page(0)
                .size(10)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        // Fixture를 사용하여 여러 Todo 생성
        List<Todo> todos = TodoFixture.todosFor(testUser, 3);
        Page<Todo> todoPage = new PageImpl<>(todos);
        
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(todoRepository.findByUserId(eq(testUser.getId()), any(Pageable.class))).thenReturn(todoPage);

        // When
        Page<com.TodoApp.backend.domain.todo.dto.TodoResponse> response = todoService.getTodos(testUser.getId(), searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(3);
        verify(userRepository).findById(testUser.getId());
        verify(todoRepository).findByUserId(eq(testUser.getId()), any(Pageable.class));
    }

    @Test
    @DisplayName("TODO 목록 조회 - 키워드 검색")
    void getTodos_키워드_검색() {
        // Given
        TodoSearchRequest searchRequest = TodoSearchRequest.builder()
                .keyword("테스트")
                .page(0)
                .size(10)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        Page<Todo> todoPage = new PageImpl<>(Arrays.asList(testTodo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.searchByKeyword(eq(1L), eq("테스트"), any(Pageable.class))).thenReturn(todoPage);

        // When
        Page<com.TodoApp.backend.domain.todo.dto.TodoResponse> response = todoService.getTodos(1L, searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(userRepository).findById(1L);
        verify(todoRepository).searchByKeyword(eq(1L), eq("테스트"), any(Pageable.class));
    }

    @Test
    @DisplayName("TODO 목록 조회 - 상태 필터")
    void getTodos_상태_필터() {
        // Given
        TodoSearchRequest searchRequest = TodoSearchRequest.builder()
                .status(Todo.TodoStatus.TODO)
                .page(0)
                .size(10)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        Page<Todo> todoPage = new PageImpl<>(Arrays.asList(testTodo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.findByUserIdAndStatus(eq(1L), eq(Todo.TodoStatus.TODO), any(Pageable.class)))
                .thenReturn(todoPage);

        // When
        Page<com.TodoApp.backend.domain.todo.dto.TodoResponse> response = todoService.getTodos(1L, searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(userRepository).findById(1L);
        verify(todoRepository).findByUserIdAndStatus(eq(1L), eq(Todo.TodoStatus.TODO), any(Pageable.class));
    }

    @Test
    @DisplayName("TODO 목록 조회 - 우선순위 필터")
    void getTodos_우선순위_필터() {
        // Given
        TodoSearchRequest searchRequest = TodoSearchRequest.builder()
                .priority(Todo.Priority.HIGH)
                .page(0)
                .size(10)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        Page<Todo> todoPage = new PageImpl<>(Arrays.asList(testTodo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.findByUserIdAndPriority(eq(1L), eq(Todo.Priority.HIGH), any(Pageable.class)))
                .thenReturn(todoPage);

        // When
        Page<com.TodoApp.backend.domain.todo.dto.TodoResponse> response = todoService.getTodos(1L, searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(userRepository).findById(1L);
        verify(todoRepository).findByUserIdAndPriority(eq(1L), eq(Todo.Priority.HIGH), any(Pageable.class));
    }

    @Test
    @DisplayName("TODO 목록 조회 - 마감일 범위 필터")
    void getTodos_마감일_범위_필터() {
        // Given
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 12, 31, 23, 59);

        TodoSearchRequest searchRequest = TodoSearchRequest.builder()
                .dueDateStart(startDate)
                .dueDateEnd(endDate)
                .page(0)
                .size(10)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        Page<Todo> todoPage = new PageImpl<>(Arrays.asList(testTodo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.findByUserIdAndDueDateBetween(eq(1L), eq(startDate), eq(endDate), any(Pageable.class)))
                .thenReturn(todoPage);

        // When
        Page<com.TodoApp.backend.domain.todo.dto.TodoResponse> response = todoService.getTodos(1L, searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(userRepository).findById(1L);
        verify(todoRepository).findByUserIdAndDueDateBetween(eq(1L), eq(startDate), eq(endDate), any(Pageable.class));
    }

    @Test
    @DisplayName("TODO 목록 조회 - 프로젝트 필터")
    void getTodos_프로젝트_필터() {
        // Given
        TodoSearchRequest searchRequest = TodoSearchRequest.builder()
                .projectId(1L)
                .page(0)
                .size(10)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        Page<Todo> todoPage = new PageImpl<>(Arrays.asList(testTodo));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(todoRepository.findByUserAndProjectId(eq(testUser), eq(1L), any(Pageable.class)))
                .thenReturn(todoPage);

        // When
        Page<com.TodoApp.backend.domain.todo.dto.TodoResponse> response = todoService.getTodos(1L, searchRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(userRepository).findById(1L);
        verify(todoRepository).findByUserAndProjectId(eq(testUser), eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("TODO 수정 성공")
    void updateTodo_성공() {
        // Given
        TodoRequest updateRequest = TodoRequest.builder()
                .title("수정된 제목")
                .description("수정된 설명")
                .status(Todo.TodoStatus.IN_PROGRESS)
                .priority(Todo.Priority.HIGH)
                .build();

        Todo updatedTodo = Todo.builder()
                .user(testUser)
                .title("수정된 제목")
                .description("수정된 설명")
                .status(Todo.TodoStatus.IN_PROGRESS)
                .priority(Todo.Priority.HIGH)
                .build();
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
    }

    @Test
    @DisplayName("TODO 수정 실패 - 권한 없음")
    void updateTodo_실패_권한_없음() {
        // Given
        TodoRequest updateRequest = TodoRequest.builder()
                .title("수정된 제목")
                .build();

        when(todoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> todoService.updateTodo(1L, 1L, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("TODO를 찾을 수 없거나 권한이 없습니다.");

        verify(todoRepository).findByIdAndUserId(1L, 1L);
        verify(todoRepository, never()).save(any(Todo.class));
    }

    @Test
    @DisplayName("TODO 상태 변경 성공")
    void updateTodoStatus_성공() {
        // Given
        Todo updatedTodo = Todo.builder()
                .user(testUser)
                .title("테스트 TODO")
                .status(Todo.TodoStatus.DONE)
                .build();
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
}

