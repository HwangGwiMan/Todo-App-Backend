package com.TodoApp.backend.global.audit.repository;

import com.TodoApp.backend.global.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 감사 로그 Repository
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 특정 엔티티의 감사 로그 조회
     */
    Page<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId, Pageable pageable);

    /**
     * 사용자별 감사 로그 조회
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * 액션별 감사 로그 조회
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);

    /**
     * 엔티티 이름과 액션으로 조회
     */
    Page<AuditLog> findByEntityNameAndAction(String entityName, String action, Pageable pageable);

    /**
     * 사용자와 액션으로 조회
     */
    Page<AuditLog> findByUserIdAndAction(Long userId, String action, Pageable pageable);

    /**
     * 기간별 감사 로그 조회
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate")
    Page<AuditLog> findByTimestampBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    /**
     * 특정 엔티티의 최신 감사 로그 조회
     */
    List<AuditLog> findTop10ByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, Long entityId);
}
