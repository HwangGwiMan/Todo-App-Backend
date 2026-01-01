package com.TodoApp.backend.domain.project.event;

import com.TodoApp.backend.domain.project.entity.Project;
import com.TodoApp.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectDeletedEvent {
    private final Project project;
    private final User user;
}