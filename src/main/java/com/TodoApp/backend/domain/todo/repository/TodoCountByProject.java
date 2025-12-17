package com.TodoApp.backend.domain.todo.repository;

/**
 * 프로젝트별 TODO 개수 집계를 위한 Projection 인터페이스
 * Spring Data JPA의 Query Projection으로 사용됩니다.
 */
public interface TodoCountByProject {
    /**
     * 프로젝트 ID
     */
    Long getProjectId();
    
    /**
     * TODO 개수
     */
    Long getCount();
}

