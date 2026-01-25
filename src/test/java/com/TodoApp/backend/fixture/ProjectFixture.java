package com.TodoApp.backend.fixture;

import com.TodoApp.backend.domain.project.entity.Project;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.fixture.core.BaseFixture;

import java.util.List;
import java.util.function.Consumer;

/**
 * Project 엔티티를 위한 Fixture
 * test-utils 기반으로 리팩토링되었습니다.
 */
public class ProjectFixture extends BaseFixture<Project, Project.ProjectBuilder> {
    
    private static final ProjectFixture INSTANCE = new ProjectFixture();
    
    private ProjectFixture() {
        super(Project.class);
    }
    
    public static ProjectFixture project() {
        return INSTANCE;
    }
    
    @Override
    protected Project.ProjectBuilder defaultBuilder() {
        // 하위 호환성을 위해 유지, 실제로는 사용되지 않음
        return Project.builder()
                .name("프로젝트 " + nextGlobalId())
                .description("프로젝트 설명")
                .color("#3B82F6")
                .isDefault(false)
                .position(0);
    }
    
    @Override
    protected Project buildFrom(Project.ProjectBuilder builder) {
        // 하위 호환성을 위해 유지, 실제로는 사용되지 않음
        return builder.build();
    }
    
    @Override
    protected void applyCustomization(Project entity, Consumer<Project.ProjectBuilder> customizer) {
        // 빌더를 사용하여 커스터마이징을 엔티티에 적용
        Project.ProjectBuilder builder = Project.builder()
                .name(entity.getName())
                .description(entity.getDescription())
                .color(entity.getColor())
                .isDefault(entity.getIsDefault())
                .position(entity.getPosition());
        customizer.accept(builder);
        Project customized = builder.build();
        
        // 엔티티에 변경사항 적용
        entity.setName(customized.getName());
        entity.setDescription(customized.getDescription());
        entity.setColor(customized.getColor());
        entity.setIsDefault(customized.getIsDefault());
        entity.setPosition(customized.getPosition());
    }
    
    @Override
    public Project aDefault() {
        // 빌더를 사용하여 기본 Project 생성
        Project project = defaultBuilder().build();
        project.setId(nextId());
        return project;
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
        Project project = project().aDefault();
        project.setName("기본 프로젝트");
        project.setIsDefault(true);
        project.setUser(user);
        return project;
    }
    
    public static Project aProjectWithColor(User user, String color) {
        Project project = project().aDefault();
        project.setColor(color);
        project.setUser(user);
        return project;
    }
    
    public static Project aProjectWithName(User user, String name) {
        Project project = project().aDefault();
        project.setName(name);
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

