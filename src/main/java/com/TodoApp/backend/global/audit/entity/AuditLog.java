package com.TodoApp.backend.global.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 감사 로그 엔티티
 * 엔티티의 변경 이력을 자동으로 기록합니다.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_entity", columnList = "entity_name, entity_id"),
    @Index(name = "idx_audit_logs_user", columnList = "user_id"),
    @Index(name = "idx_audit_logs_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 엔티티 이름 (예: "Todo", "Project")
     */
    @Column(name = "entity_name", nullable = false, length = 50)
    private String entityName;

    /**
     * 엔티티 ID
     */
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    /**
     * 수행된 액션 (CREATE, UPDATE, DELETE)
     */
    @Column(nullable = false, length = 20)
    private String action;

    /**
     * 사용자 ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 사용자명
     */
    @Column(nullable = false, length = 100)
    private String username;

    /**
     * 변경 전 데이터 (JSON 형식)
     * CREATE의 경우 null
     */
    @Column(name = "changes_before", columnDefinition = "TEXT")
    private String changesBefore;

    /**
     * 변경 후 데이터 (JSON 형식)
     * DELETE의 경우 null
     */
    @Column(name = "changes_after", columnDefinition = "TEXT")
    private String changesAfter;

    /**
     * 변경 시각
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * 요청 IP 주소
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}
