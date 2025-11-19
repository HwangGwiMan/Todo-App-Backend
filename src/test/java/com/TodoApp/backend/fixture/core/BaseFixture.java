package com.TodoApp.backend.fixture.core;

import com.TodoApp.backend.global.entity.BaseEntity;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 모든 Fixture의 기본 추상 클래스
 * 공통 기능(ID 생성, 빌더 패턴 등)을 제공
 * 
 * @param <T> 엔티티 타입
 * @param <B> 빌더 타입
 */
public abstract class BaseFixture<T extends BaseEntity, B> {
    
    protected static final AtomicLong globalIdGenerator = new AtomicLong(1);
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * 기본 빌더를 반환 (하위 클래스에서 구현)
     */
    protected abstract B defaultBuilder();
    
    /**
     * 빌더로부터 엔티티를 생성 (하위 클래스에서 구현)
     */
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
     * 기본 엔티티 생성
     */
    public T aDefault() {
        T entity = buildFrom(defaultBuilder());
        entity.setId(nextId());
        return entity;
    }
    
    /**
     * 커스터마이징 가능한 엔티티 생성
     */
    public T a(Consumer<B> customizer) {
        B builder = defaultBuilder();
        customizer.accept(builder);
        T entity = buildFrom(builder);
        entity.setId(nextId());
        return entity;
    }
    
    /**
     * 여러 개 생성
     */
    public List<T> many(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> aDefault())
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
}

