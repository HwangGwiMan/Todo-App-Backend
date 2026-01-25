package com.TodoApp.backend.fixture.core;

import com.TodoApp.backend.domain.user.entity.User;
import com.core.test.utils.TestSupport;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 확장 가능한 테스트 데이터 그래프 빌더
 * test-utils 기반으로 재구현되었습니다.
 * 새로운 엔티티가 추가되어도 쉽게 확장 가능
 */
public class TestDataGraph {
    
    private final Map<Class<?>, List<?>> entities = new HashMap<>();
    private final Map<String, Object> namedEntities = new HashMap<>();
    private final Map<Class<?>, TestSupport<?>> testSupports = new HashMap<>();
    
    // User 관련
    private User user;
    
    /**
     * 엔티티 추가
     */
    @SuppressWarnings("unchecked")
    public <T> TestDataGraph with(Class<T> entityClass, T entity) {
        List<Object> list = (List<Object>) entities.computeIfAbsent(entityClass, k -> new ArrayList<>());
        list.add(entity);
        return this;
    }
    
    /**
     * 여러 엔티티 추가
     */
    @SuppressWarnings("unchecked")
    public <T> TestDataGraph withMany(Class<T> entityClass, List<T> entities) {
        List<Object> list = (List<Object>) this.entities.computeIfAbsent(entityClass, k -> new ArrayList<>());
        list.addAll(entities);
        return this;
    }
    
    /**
     * 이름을 가진 엔티티 추가
     */
    public <T> TestDataGraph withNamed(String name, T entity) {
        namedEntities.put(name, entity);
        return this;
    }
    
    /**
     * User 설정
     */
    public TestDataGraph withUser(User user) {
        this.user = user;
        return with(User.class, user);
    }
    
    /**
     * User를 빌더로부터 생성하여 설정 (하위 호환성 유지)
     */
    public TestDataGraph withUser(Function<com.TodoApp.backend.fixture.UserFixture, User> userBuilder) {
        User user = userBuilder.apply(com.TodoApp.backend.fixture.UserFixture.user());
        return withUser(user);
    }
    
    /**
     * test-utils를 사용하여 엔티티 생성 및 추가
     */
    @SuppressWarnings("unchecked")
    public <T> TestDataGraph withEntity(Class<T> entityClass) {
        TestSupport<T> support = (TestSupport<T>) testSupports.computeIfAbsent(
                entityClass, 
                k -> new TestSupport<>(entityClass)
        );
        T entity = null;
        try {
            entity = support.monkey();
        } catch (NoSuchMethodError | NullPointerException e) {
            // monkey() 메서드가 존재하지 않거나 null을 반환하는 경우
            entity = null;
        }
        if (entity == null) {
            throw new UnsupportedOperationException("TestSupport.monkey() is not available. Use Fixture classes instead.");
        }
        return with(entityClass, entity);
    }
    
    /**
     * test-utils를 사용하여 커스터마이징된 엔티티 생성 및 추가
     */
    @SuppressWarnings("unchecked")
    public <T> TestDataGraph withEntity(Class<T> entityClass, Consumer<T> customizer) {
        TestSupport<T> support = (TestSupport<T>) testSupports.computeIfAbsent(
                entityClass, 
                k -> new TestSupport<>(entityClass)
        );
        T entity = null;
        try {
            entity = support.monkey();
        } catch (NoSuchMethodError | NullPointerException e) {
            // monkey() 메서드가 존재하지 않거나 null을 반환하는 경우
            entity = null;
        }
        if (entity == null) {
            throw new UnsupportedOperationException("TestSupport.monkey() is not available. Use Fixture classes instead.");
        }
        if (customizer != null) {
            customizer.accept(entity);
        }
        return with(entityClass, entity);
    }
    
    /**
     * test-utils를 사용하여 여러 엔티티 생성 및 추가
     */
    @SuppressWarnings("unchecked")
    public <T> TestDataGraph withManyEntities(Class<T> entityClass, int count) {
        TestSupport<T> support = (TestSupport<T>) testSupports.computeIfAbsent(
                entityClass, 
                k -> new TestSupport<>(entityClass)
        );
        List<T> entities = support.monkeyList(count);
        return withMany(entityClass, entities);
    }
    
    /**
     * 엔티티 목록 조회
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> get(Class<T> entityClass) {
        return (List<T>) entities.getOrDefault(entityClass, Collections.emptyList());
    }
    
    /**
     * 첫 번째 엔티티 조회
     */
    @SuppressWarnings("unchecked")
    public <T> T getFirst(Class<T> entityClass) {
        List<T> list = get(entityClass);
        return list.isEmpty() ? null : list.get(0);
    }
    
    /**
     * 이름으로 엔티티 조회
     */
    @SuppressWarnings("unchecked")
    public <T> T getNamed(String name, Class<T> type) {
        return (T) namedEntities.get(name);
    }
    
    /**
     * User 조회
     */
    public User getUser() {
        return user;
    }
    
    /**
     * 연관 관계 자동 설정 헬퍼
     */
    public <T> TestDataGraph link(Class<T> entityClass, Consumer<T> linker) {
        get(entityClass).forEach(linker::accept);
        return this;
    }
    
    /**
     * 그래프 생성 팩토리 메서드
     */
    public static TestDataGraph create() {
        return new TestDataGraph();
    }
}

