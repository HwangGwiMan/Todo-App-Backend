package com.TodoApp.backend.global.audit.service;

import com.TodoApp.backend.global.audit.entity.AuditLog;
import com.TodoApp.backend.global.audit.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 감사 로그 서비스
 * 비동기 처리를 통해 메인 트랜잭션에 영향을 주지 않도록 합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    
    /**
     * Jackson ObjectMapper (순환 참조 방지 설정 포함)
     */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    /**
     * 감사 로그를 비동기로 저장합니다.
     * 별도 트랜잭션으로 처리하여 메인 트랜잭션이 롤백되어도 감사 로그는 보존됩니다.
     * 
     * @param entityName 엔티티 이름
     * @param entityId 엔티티 ID
     * @param action 액션 (CREATE, UPDATE, DELETE)
     * @param userId 사용자 ID
     * @param username 사용자명
     * @param changesBefore 변경 전 데이터 (JSON)
     * @param changesAfter 변경 후 데이터 (JSON)
     * @param ipAddress IP 주소
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditLog(
            String entityName,
            Long entityId,
            String action,
            Long userId,
            String username,
            String changesBefore,
            String changesAfter,
            String ipAddress
    ) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityName(entityName)
                    .entityId(entityId)
                    .action(action)
                    .userId(userId)
                    .username(username)
                    .changesBefore(changesBefore)
                    .changesAfter(changesAfter)
                    .timestamp(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("감사 로그 저장 완료: entityName={}, entityId={}, action={}", 
                    entityName, entityId, action);
        } catch (Exception e) {
            // 감사 로그 저장 실패가 메인 트랜잭션에 영향을 주지 않도록 예외를 로깅만 함
            log.error("감사 로그 저장 실패: entityName={}, entityId={}, action={}", 
                    entityName, entityId, action, e);
        }
    }

    /**
     * 엔티티를 JSON 문자열로 직렬화합니다.
     * 
     * @param entity 직렬화할 엔티티
     * @return JSON 문자열 (실패 시 null)
     */
    public String serializeEntity(Object entity) {
        if (entity == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            log.warn("엔티티 직렬화 실패: {}", entity.getClass().getSimpleName(), e);
            return null;
        }
    }

    /**
     * 특정 엔티티의 감사 로그 조회
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> findByEntity(String entityName, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityNameAndEntityId(entityName, entityId, pageable);
    }

    /**
     * 사용자별 감사 로그 조회
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> findByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    /**
     * 액션별 감사 로그 조회
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> findByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }

    /**
     * 엔티티 이름과 액션으로 조회
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> findByEntityNameAndAction(String entityName, String action, Pageable pageable) {
        return auditLogRepository.findByEntityNameAndAction(entityName, action, pageable);
    }

    /**
     * 사용자와 액션으로 조회
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> findByUserIdAndAction(Long userId, String action, Pageable pageable) {
        return auditLogRepository.findByUserIdAndAction(userId, action, pageable);
    }

    /**
     * 기간별 감사 로그 조회
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
    }

    /**
     * 특정 엔티티의 최신 감사 로그 조회 (최대 10개)
     */
    @Transactional(readOnly = true)
    public List<AuditLog> findRecentByEntity(String entityName, Long entityId) {
        return auditLogRepository.findTop10ByEntityNameAndEntityIdOrderByTimestampDesc(entityName, entityId);
    }

    /**
     * 감사 로그 상세 조회
     */
    @Transactional(readOnly = true)
    public AuditLog findById(Long id) {
        return auditLogRepository.findById(id)
                .orElse(null);
    }
}
