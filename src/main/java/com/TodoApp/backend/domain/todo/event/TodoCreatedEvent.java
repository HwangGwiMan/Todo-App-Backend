package com.TodoApp.backend.domain.todo.event;

import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TODO 생성 이벤트
 */
@Getter
@AllArgsConstructor
public class TodoCreatedEvent {
    private final Todo todo;
    private final User user;
}

