package com.TodoApp.backend.fixture.core;

import com.TodoApp.backend.domain.project.entity.Project;
import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.fixture.ProjectFixture;
import com.TodoApp.backend.fixture.TodoFixture;
import com.TodoApp.backend.fixture.UserFixture;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 모든 Fixture를 중앙에서 관리하는 레지스트리
 * 새로운 Fixture 추가 시 자동으로 등록됨
 */
public class FixtureRegistry {
    
    private static final Map<Class<?>, BaseFixture<?, ?>> fixtures = new ConcurrentHashMap<>();
    
    static {
        // 기본 Fixture 등록
        register(User.class, UserFixture.user());
        register(Todo.class, TodoFixture.todo());
        register(Project.class, ProjectFixture.project());
    }
    
    /**
     * Fixture 조회
     */
    @SuppressWarnings("unchecked")
    public static <T extends com.TodoApp.backend.global.entity.BaseEntity, B> BaseFixture<T, B> get(Class<T> entityClass) {
        return (BaseFixture<T, B>) fixtures.get(entityClass);
    }
    
    /**
     * Fixture 등록
     */
    public static <T extends com.TodoApp.backend.global.entity.BaseEntity, B> void register(Class<T> entityClass, BaseFixture<T, B> fixture) {
        fixtures.put(entityClass, fixture);
    }
    
    /**
     * 등록된 모든 Fixture 클래스 조회
     */
    public static java.util.Set<Class<?>> getRegisteredEntityClasses() {
        return fixtures.keySet();
    }
}

