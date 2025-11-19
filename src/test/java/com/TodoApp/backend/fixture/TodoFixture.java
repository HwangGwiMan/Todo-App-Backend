package com.TodoApp.backend.fixture;

import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.fixture.core.BaseFixture;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * Todo 엔티티를 위한 Fixture
 */
public class TodoFixture extends BaseFixture<Todo, Todo.TodoBuilder> {
    
    private static final TodoFixture INSTANCE = new TodoFixture();
    
    public static TodoFixture todo() {
        return INSTANCE;
    }
    
    @Override
    protected Todo.TodoBuilder defaultBuilder() {
        return Todo.builder()
                .title("테스트 TODO " + nextGlobalId())
                .description("테스트 설명")
                .status(Todo.TodoStatus.TODO)
                .priority(Todo.Priority.MEDIUM)
                .position(0);
    }
    
    @Override
    protected Todo buildFrom(Todo.TodoBuilder builder) {
        return builder.build();
    }
    
    // 연관 관계를 포함한 생성 메서드
    public static Todo aTodoFor(User user) {
        Todo todo = todo().aDefault();
        todo.setUser(user);
        return todo;
    }
    
    public static Todo aTodoFor(User user, Consumer<Todo.TodoBuilder> customizer) {
        Todo todo = todo().a(customizer);
        todo.setUser(user);
        return todo;
    }
    
    public static Todo aCompletedTodoFor(User user) {
        Todo todo = todo().a(builder -> builder.status(Todo.TodoStatus.DONE));
        todo.setUser(user);
        return todo;
    }
    
    public static Todo aHighPriorityTodoFor(User user) {
        Todo todo = todo().a(builder -> builder.priority(Todo.Priority.HIGH));
        todo.setUser(user);
        return todo;
    }
    
    public static Todo aLowPriorityTodoFor(User user) {
        Todo todo = todo().a(builder -> builder.priority(Todo.Priority.LOW));
        todo.setUser(user);
        return todo;
    }
    
    public static Todo anInProgressTodoFor(User user) {
        Todo todo = todo().a(builder -> builder.status(Todo.TodoStatus.IN_PROGRESS));
        todo.setUser(user);
        return todo;
    }
    
    public static Todo anOverdueTodoFor(User user) {
        Todo todo = todo().a(builder -> builder
                .dueDate(Timestamp.valueOf(LocalDateTime.now().minusDays(1)))
                .status(Todo.TodoStatus.TODO));
        todo.setUser(user);
        return todo;
    }
    
    public static Todo aTodoWithProject(User user, Long projectId) {
        Todo todo = todo().aDefault();
        todo.setUser(user);
        todo.setProjectId(projectId);
        return todo;
    }
    
    /**
     * 연관 관계를 포함한 TODO 목록 생성
     */
    public static List<Todo> todosFor(User user, int count) {
        List<Todo> todos = todo().many(count);
        todos.forEach(todo -> todo.setUser(user));
        return todos;
    }
    
    /**
     * 다양한 상태의 TODO 생성
     */
    public static List<Todo> todosWithVariousStatuses(User user) {
        return List.of(
            aTodoFor(user),
            anInProgressTodoFor(user),
            aCompletedTodoFor(user)
        );
    }
    
    /**
     * 다양한 우선순위의 TODO 생성
     */
    public static List<Todo> todosWithVariousPriorities(User user) {
        return List.of(
            aHighPriorityTodoFor(user),
            aTodoFor(user), // MEDIUM
            aLowPriorityTodoFor(user)
        );
    }
}

