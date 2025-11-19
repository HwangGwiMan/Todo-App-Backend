package com.TodoApp.backend.fixture;

import com.TodoApp.backend.domain.project.entity.Project;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.fixture.core.BaseFixture;

import java.util.List;
import java.util.function.Consumer;

/**
 * Project 엔티티를 위한 Fixture
 */
public class ProjectFixture extends BaseFixture<Project, Project.ProjectBuilder> {
    
    private static final ProjectFixture INSTANCE = new ProjectFixture();
    
    public static ProjectFixture project() {
        return INSTANCE;
    }
    
    @Override
    protected Project.ProjectBuilder defaultBuilder() {
        return Project.builder()
                .name("프로젝트 " + nextGlobalId())
                .description("프로젝트 설명")
                .color("#3B82F6")
                .isDefault(false)
                .position(0);
    }
    
    @Override
    protected Project buildFrom(Project.ProjectBuilder builder) {
        return builder.build();
    }
    
    // 연관 관계를 포함한 생성 메서드
    public static Project aProjectFor(User user) {
        Project project = project().aDefault();
        project.setUser(user);
        return project;
    }
    
    public static Project aProjectFor(User user, Consumer<Project.ProjectBuilder> customizer) {
        Project project = project().a(customizer);
        project.setUser(user);
        return project;
    }
    
    public static Project aDefaultProjectFor(User user) {
        Project project = project().a(builder -> builder
                .name("기본 프로젝트")
                .isDefault(true));
        project.setUser(user);
        return project;
    }
    
    public static Project aProjectWithColor(User user, String color) {
        Project project = project().a(builder -> builder.color(color));
        project.setUser(user);
        return project;
    }
    
    public static Project aProjectWithName(User user, String name) {
        Project project = project().a(builder -> builder.name(name));
        project.setUser(user);
        return project;
    }
    
    /**
     * 연관 관계를 포함한 프로젝트 목록 생성
     */
    public static List<Project> projectsFor(User user, int count) {
        List<Project> projects = project().many(count);
        projects.forEach(project -> project.setUser(user));
        return projects;
    }
}

