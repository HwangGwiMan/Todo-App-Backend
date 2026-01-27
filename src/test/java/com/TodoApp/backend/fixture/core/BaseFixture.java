package com.TodoApp.backend.fixture.core;

import com.TodoApp.backend.global.entity.BaseEntity;
import com.core.test.utils.TestSupport;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 모든 Fixture의 기본 추상 클래스
 * test-utils의 TestSupport를 활용하여 테스트 데이터를 생성합니다.
 * 기존 API 호환성을 유지하면서 내부 구현만 변경되었습니다.
 * 
 * @param <T> 엔티티 타입
 * @param <B> 빌더 타입 (하위 호환성을 위해 유지)
 */
public abstract class BaseFixture<T extends BaseEntity, B> {
    
    protected static final AtomicLong globalIdGenerator = new AtomicLong(1);
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final TestSupport<T> testSupport;
    
    /**
     * 생성자 - 엔티티 클래스를 받아 TestSupport 초기화
     */
    @SuppressWarnings("unchecked")
    protected BaseFixture(Class<T> entityClass) {
        this.testSupport = new TestSupport<>(entityClass);
    }
    
    /**
     * 기본 빌더를 반환 (하위 호환성을 위해 유지, 실제로는 사용되지 않음)
     * @deprecated test-utils 사용으로 인해 더 이상 사용되지 않습니다.
     */
    @Deprecated
    protected abstract B defaultBuilder();
    
    /**
     * 빌더로부터 엔티티를 생성 (하위 호환성을 위해 유지, 실제로는 사용되지 않음)
     * @deprecated test-utils 사용으로 인해 더 이상 사용되지 않습니다.
     */
    @Deprecated
    protected abstract T buildFrom(B builder);
    
    /**
     * 고유한 ID 생성
     */
    protected Long nextId() {
        return idGenerator.getAndIncrement();
    }
    
    /**
     * 전역 고유 ID 생성 (여러 Fixture 간 충돌 방지)
     */
    protected static Long nextGlobalId() {
        return globalIdGenerator.getAndIncrement();
    }
    
    /**
     * 기본 엔티티 생성 (test-utils 기반)
     * 하위 클래스에서 오버라이드하여 구현해야 합니다.
     */
    @SuppressWarnings("unchecked")
    public abstract T aDefault();
    
    /**
     * 커스터마이징 가능한 엔티티 생성 (빌더 기반)
     */
    @SuppressWarnings("unchecked")
    public T a(Consumer<B> customizer) {
        // 빌더를 사용하여 엔티티 생성 후 커스터마이징
        B builder = defaultBuilder();
        if (customizer != null) {
            customizer.accept(builder);
        }
        T entity = (T) buildFrom(builder);
        entity.setId(nextId());
        return entity;
    }
    
    /**
     * 커스터마이징을 엔티티에 적용 (하위 클래스에서 오버라이드 가능)
     */
    protected void applyCustomization(T entity, Consumer<B> customizer) {
        // 기본 구현: 하위 클래스에서 오버라이드하여 처리
        // 예: UserFixture에서는 User.UserBuilder를 사용하여 커스터마이징
    }
    
    /**
     * 여러 개 생성
     */
    @SuppressWarnings("unchecked")
    public List<T> many(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    T entity = (T) buildFrom(defaultBuilder());
                    entity.setId(nextId());
                    return entity;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 여러 개 생성 (커스터마이징)
     */
    public List<T> many(int count, Consumer<B> customizer) {
        return IntStream.range(0, count)
                .mapToObj(i -> a(customizer))
                .collect(Collectors.toList());
    }
    
    /**
     * TestSupport 인스턴스 접근 (하위 클래스에서 사용)
     */
    protected TestSupport<T> getTestSupport() {
        return testSupport;
    }
}

