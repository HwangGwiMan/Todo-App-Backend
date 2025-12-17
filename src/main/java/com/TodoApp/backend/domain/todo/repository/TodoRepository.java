package com.TodoApp.backend.domain.todo.repository;

import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long>, TodoRepositoryCustom {

    // 사용자별 TODO 조회 (페이징)
    Page<Todo> findByUserId(Long userId, Pageable pageable);

    // 사용자 + 상태별 조회
    Page<Todo> findByUserIdAndStatus(Long userId, Todo.TodoStatus status, Pageable pageable);

    // 사용자 + 우선순위별 조회
    Page<Todo> findByUserIdAndPriority(Long userId, Todo.Priority priority, Pageable pageable);

    // 사용자별 TODO 단건 조회 (권한 확인용)
    Optional<Todo> findByIdAndUserId(Long id, Long userId);

    // 사용자별 검색 (제목 또는 설명에서)
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Todo> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    // 마감일 범위로 조회
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId AND t.dueDate BETWEEN :startDate AND :endDate")
    Page<Todo> findByUserIdAndDueDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    // 마감일이 지난 TODO 조회
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId AND t.dueDate < :now AND t.status != 'DONE'")
    List<Todo> findOverdueTodos(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    // 사용자별 통계 - 총 개수
    long countByUserId(Long userId);

    // 사용자별 상태별 개수
    long countByUserIdAndStatus(Long userId, Todo.TodoStatus status);

    // 사용자 삭제 시 CASCADE 처리를 위한 삭제 메서드
    void deleteByUserId(Long userId);

    // 프로젝트 관련 메서드들
    
    // 사용자와 프로젝트 ID별 TODO 개수
    long countByUserAndProjectId(User user, Long projectId);
    
    // 사용자별 프로젝트가 null인 TODO 개수
    long countByUserAndProjectIdIsNull(User user);
    
    /**
     * 사용자별 프로젝트 ID로 그룹화한 TODO 개수 조회
     * N+1 문제 해결을 위한 메서드
     * 
     * @param userId 사용자 ID
     * @return 프로젝트 ID별 TODO 개수 목록
     */
    @Query("""
        SELECT t.projectId as projectId, COUNT(t) as count
        FROM Todo t 
        WHERE t.user.id = :userId 
        GROUP BY t.projectId
        """)
    List<TodoCountByProject> countByUserGroupByProjectId(@Param("userId") Long userId);
    
    // 프로젝트 ID를 null로 업데이트 (프로젝트 삭제 시)
    @Modifying
    @Query("UPDATE Todo t SET t.projectId = null WHERE t.projectId = :projectId")
    void updateProjectIdToNullByProjectId(@Param("projectId") Long projectId);
    
    // 사용자 + 프로젝트별 조회
    Page<Todo> findByUserAndProjectId(User user, Long projectId, Pageable pageable);
    
    // 사용자별 프로젝트가 null인 TODO 조회
    Page<Todo> findByUserAndProjectIdIsNull(User user, Pageable pageable);
}

