package com.TodoApp.backend.domain.todo.repository.specification;

import com.TodoApp.backend.domain.todo.entity.Todo;
import org.springframework.data.jpa.domain.Specification;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * TODO 엔티티를 위한 JPA Specification 클래스
 * 동적 쿼리 생성 및 복합 필터 조합을 위한 명세 패턴 구현
 */
public class TodoSpecification {

    /**
     * 사용자 ID 필터
     * 
     * @param userId 사용자 ID
     * @return 사용자 ID가 일치하는 Specification
     */
    public static Specification<Todo> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction(); // true 반환
            }
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    /**
     * 키워드 검색 (제목 또는 설명)
     * 
     * @param keyword 검색 키워드
     * @return 제목이나 설명에 키워드가 포함된 Specification
     */
    public static Specification<Todo> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction(); // 조건 없음
            }
            
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), 
                    likePattern
                ),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), 
                    likePattern
                )
            );
        };
    }

    /**
     * 상태 필터
     * 
     * @param status TODO 상태
     * @return 상태가 일치하는 Specification
     */
    public static Specification<Todo> hasStatus(Todo.TodoStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    /**
     * 우선순위 필터
     * 
     * @param priority TODO 우선순위
     * @return 우선순위가 일치하는 Specification
     */
    public static Specification<Todo> hasPriority(Todo.Priority priority) {
        return (root, query, criteriaBuilder) -> {
            if (priority == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("priority"), priority);
        };
    }

    /**
     * 프로젝트 ID 필터
     * 
     * @param projectId 프로젝트 ID
     * @return 프로젝트 ID가 일치하는 Specification
     */
    public static Specification<Todo> hasProjectId(Long projectId) {
        return (root, query, criteriaBuilder) -> {
            if (projectId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("projectId"), projectId);
        };
    }

    /**
     * 마감일 범위 필터
     * 
     * @param startDate 시작 일시
     * @param endDate 종료 일시
     * @return 마감일이 범위 내에 있는 Specification
     */
    public static Specification<Todo> dueDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null || endDate == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.between(root.get("dueDate"), startDate, endDate);
        };
    }

    /**
     * 마감일이 특정 일시 이전인 필터
     * 
     * @param date 기준 일시
     * @return 마감일이 기준 일시 이전인 Specification
     */
    public static Specification<Todo> dueDateBefore(Timestamp date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThan(root.get("dueDate"), date);
        };
    }

    /**
     * 마감일이 특정 일시 이후인 필터
     * 
     * @param date 기준 일시
     * @return 마감일이 기준 일시 이후인 Specification
     */
    public static Specification<Todo> dueDateAfter(Timestamp date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get("dueDate"), date);
        };
    }

    /**
     * 완료되지 않은 TODO 필터
     * 
     * @return 상태가 DONE이 아닌 Specification
     */
    public static Specification<Todo> isNotCompleted() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.notEqual(root.get("status"), Todo.TodoStatus.DONE);
    }

    /**
     * 완료된 TODO 필터
     * 
     * @return 상태가 DONE인 Specification
     */
    public static Specification<Todo> isCompleted() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("status"), Todo.TodoStatus.DONE);
    }
}
