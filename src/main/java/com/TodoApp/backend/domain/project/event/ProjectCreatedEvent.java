package com.TodoApp.backend.domain.project.event;

import com.TodoApp.backend.domain.project.entity.Project;
import com.TodoApp.backend.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 프로젝트 생성 이벤트
 */
@Getter
@AllArgsConstructor
public class ProjectCreatedEvent {
    private final Project project;
    private final User user;
}

