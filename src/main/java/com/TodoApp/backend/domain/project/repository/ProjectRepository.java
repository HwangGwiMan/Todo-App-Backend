package com.TodoApp.backend.domain.project.repository;

import com.TodoApp.backend.domain.project.entity.Project;
import com.TodoApp.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * 사용자별 프로젝트 목록 조회 (정렬 순서별)
     */
    List<Project> findByUserOrderByPositionAscCreatedAtAsc(User user);
    
    /**
     * 사용자별 프로젝트 개수 조회
     */
    long countByUser(User user);
    
    /**
     * 사용자의 기본 프로젝트 조회
     */
    Optional<Project> findByUserAndIsDefaultTrue(User user);
    
    /**
     * 사용자와 프로젝트 ID로 프로젝트 조회
     */
    Optional<Project> findByIdAndUser(Long id, User user);
    
    /**
     * 사용자별 프로젝트명 중복 체크
     */
    boolean existsByUserAndName(User user, String name);
    
    /**
     * 사용자별 프로젝트명 중복 체크 (수정 시)
     */
    @Query("SELECT COUNT(p) > 0 FROM Project p WHERE p.user = :user AND p.name = :name AND p.id != :excludeId")
    boolean existsByUserAndNameExcludingId(@Param("user") User user, @Param("name") String name, @Param("excludeId") Long excludeId);
    
    /**
     * 사용자별 최대 position 값 조회
     */
    @Query("SELECT COALESCE(MAX(p.position), 0) FROM Project p WHERE p.user = :user")
    Integer findMaxPositionByUser(@Param("user") User user);
}
