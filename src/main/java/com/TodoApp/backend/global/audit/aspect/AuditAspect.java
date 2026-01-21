package com.TodoApp.backend.global.audit.aspect;

import com.TodoApp.backend.domain.todo.repository.TodoRepository;
import com.TodoApp.backend.domain.project.repository.ProjectRepository;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.global.audit.annotation.Auditable;
import com.TodoApp.backend.global.audit.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 감사 로그를 자동으로 기록하는 AOP Aspect
 * 
 * @Order(1)을 설정하여 다른 Aspect보다 먼저 실행되도록 합니다.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final TodoRepository todoRepository;
    private final ProjectRepository projectRepository;
    
    /**
     * ThreadLocal을 사용하여 변경 전 데이터를 저장
     * UPDATE 액션의 경우 변경 전 엔티티를 저장하기 위해 사용
     */
    private static final ThreadLocal<Map<String, Object>> BEFORE_DATA = new ThreadLocal<>();

    /**
     * UPDATE 액션의 경우 변경 전 데이터를 캡처
     */
    @Around("@annotation(auditable)")
    public Object captureBeforeData(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String action = auditable.action();
        
        // UPDATE 액션인 경우 변경 전 데이터 캡처
        if ("UPDATE".equals(action)) {
            try {
                // 파라미터에서 엔티티 ID 추출
                Long entityId = extractEntityIdFromArgs(joinPoint);
                String entityName = determineEntityNameFromMethod(joinPoint);
                
                if (entityId != null && entityName != null) {
                    // Repository를 통해 변경 전 엔티티 조회
                    Object entity = fetchEntityBeforeUpdate(entityName, entityId, joinPoint);
                    if (entity != null) {
                        Map<String, Object> beforeData = new HashMap<>();
                        beforeData.put("entity", entity);
                        BEFORE_DATA.set(beforeData);
                    }
                }
            } catch (Exception e) {
                log.warn("변경 전 데이터 캡처 실패: {}", e.getMessage());
            }
        } else if ("DELETE".equals(action)) {
            // DELETE 액션의 경우도 변경 전 데이터 캡처
            try {
                Long entityId = extractEntityIdFromArgs(joinPoint);
                String entityName = determineEntityNameFromMethod(joinPoint);
                
                if (entityId != null && entityName != null) {
                    Object entity = fetchEntityBeforeUpdate(entityName, entityId, joinPoint);
                    if (entity != null) {
                        Map<String, Object> beforeData = new HashMap<>();
                        beforeData.put("entity", entity);
                        BEFORE_DATA.set(beforeData);
                    }
                }
            } catch (Exception e) {
                log.warn("삭제 전 데이터 캡처 실패: {}", e.getMessage());
            }
        }
        
        // 메서드 실행
        return joinPoint.proceed();
    }

    /**
     * 메서드 실행 후 감사 로그 기록
     */
    @AfterReturning(
            pointcut = "@annotation(auditable)",
            returning = "result"
    )
    public void logAudit(ProceedingJoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            String action = auditable.action();
            String entityName = determineEntityName(auditable, joinPoint, result);
            Long entityId = extractEntityId(result, joinPoint);
            
            if (entityName == null || entityId == null) {
                log.warn("엔티티 정보를 추출할 수 없습니다. entityName={}, entityId={}", entityName, entityId);
                return;
            }

            // 사용자 정보 추출
            User user = getCurrentUser();
            if (user == null) {
                log.warn("현재 사용자 정보를 찾을 수 없습니다.");
                return;
            }

            // 변경 전/후 데이터 추출
            String changesBefore = null;
            String changesAfter = null;

            if ("UPDATE".equals(action)) {
                // 변경 전 데이터
                Map<String, Object> beforeData = BEFORE_DATA.get();
                if (beforeData != null && beforeData.containsKey("entity")) {
                    changesBefore = auditLogService.serializeEntity(beforeData.get("entity"));
                }
                // 변경 후 데이터
                changesAfter = auditLogService.serializeEntity(result);
            } else if ("CREATE".equals(action)) {
                // 생성의 경우 변경 후 데이터만
                changesAfter = auditLogService.serializeEntity(result);
            } else if ("DELETE".equals(action)) {
                // 삭제의 경우 변경 전 데이터만 (result는 void일 수 있음)
                Map<String, Object> beforeData = BEFORE_DATA.get();
                if (beforeData != null && beforeData.containsKey("entity")) {
                    changesBefore = auditLogService.serializeEntity(beforeData.get("entity"));
                }
            }

            // IP 주소 추출
            String ipAddress = getClientIpAddress();

            // 감사 로그 저장 (비동기)
            auditLogService.saveAuditLog(
                    entityName,
                    entityId,
                    action,
                    user.getId(),
                    user.getUsername(),
                    changesBefore,
                    changesAfter,
                    ipAddress
            );

        } catch (Exception e) {
            // 감사 로그 기록 실패가 메인 로직에 영향을 주지 않도록 예외를 로깅만 함
            log.error("감사 로그 기록 실패", e);
        } finally {
            // ThreadLocal 정리
            BEFORE_DATA.remove();
        }
    }

    /**
     * 엔티티 이름 결정
     * 1. 어노테이션의 entityName이 지정되어 있으면 사용
     * 2. 반환 타입에서 추출 시도
     * 3. 파라미터에서 추출 시도
     */
    private String determineEntityName(Auditable auditable, ProceedingJoinPoint joinPoint, Object result) {
        // 1. 어노테이션에 명시된 경우
        if (auditable.entityName() != null && !auditable.entityName().isEmpty()) {
            return auditable.entityName();
        }

        // 2. 반환 타입에서 추출
        if (result != null) {
            String entityName = extractEntityNameFromClass(result.getClass());
            if (entityName != null) {
                return entityName;
            }
        }

        // 3. 메서드 시그니처에서 반환 타입 추출
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();
        String entityName = extractEntityNameFromClass(returnType);
        if (entityName != null) {
            return entityName;
        }

        // 4. 파라미터에서 추출 시도
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg != null) {
                String name = extractEntityNameFromClass(arg.getClass());
                if (name != null) {
                    return name;
                }
            }
        }

        return null;
    }

    /**
     * 클래스에서 엔티티 이름 추출
     */
    private String extractEntityNameFromClass(Class<?> clazz) {
        // Response DTO인 경우 내부 엔티티 타입 추출 시도
        String className = clazz.getSimpleName();
        
        // Response DTO에서 엔티티 타입 추출
        if (className.endsWith("Response")) {
            String entityName = className.substring(0, className.length() - 8); // "Response" 제거
            if (isKnownEntity(entityName)) {
                return entityName;
            }
        }
        
        // 직접 엔티티 클래스인 경우
        if (isKnownEntity(className)) {
            return className;
        }
        
        return null;
    }

    /**
     * 알려진 엔티티인지 확인
     */
    private boolean isKnownEntity(String entityName) {
        return "Todo".equals(entityName) || "Project".equals(entityName);
    }

    /**
     * 엔티티 ID 추출
     */
    private Long extractEntityId(Object result, ProceedingJoinPoint joinPoint) {
        // 1. 반환값에서 ID 추출 시도
        if (result != null) {
            try {
                Method getIdMethod = result.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(result);
                if (id instanceof Long) {
                    return (Long) id;
                }
            } catch (Exception e) {
                // getId() 메서드가 없거나 접근 불가능한 경우
            }
        }

        // 2. 파라미터에서 ID 추출 시도 (예: updateTodo(Long todoId, ...))
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }

        return null;
    }

    /**
     * 파라미터에서 엔티티 ID 추출
     * Service 메서드의 파라미터 순서를 고려하여 ID를 찾습니다.
     * 일반적으로 updateTodo(Long todoId, ...) 또는 updateProject(Long projectId, ...) 형태
     */
    private Long extractEntityIdFromArgs(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        
        // 메서드명에서 엔티티 타입 추정
        boolean isTodo = methodName.contains("Todo");
        boolean isProject = methodName.contains("Project");
        
        // 파라미터에서 Long 타입 ID 찾기
        // 일반적으로 두 번째 파라미터가 ID (첫 번째는 userId)
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Long) {
                Long id = (Long) args[i];
                // 첫 번째 파라미터는 보통 userId이므로 두 번째 이후를 우선
                if (i > 0 || (isTodo || isProject)) {
                    return id;
                }
            }
        }
        
        return null;
    }

    /**
     * 메서드명에서 엔티티 이름 추정
     */
    private String determineEntityNameFromMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        
        if (methodName.contains("Todo")) {
            return "Todo";
        } else if (methodName.contains("Project")) {
            return "Project";
        }
        
        return null;
    }

    /**
     * Repository를 통해 변경 전 엔티티 조회
     */
    private Object fetchEntityBeforeUpdate(String entityName, Long entityId, ProceedingJoinPoint joinPoint) {
        try {
            // 현재 사용자 정보 필요 (권한 체크)
            User user = getCurrentUser();
            if (user == null) {
                return null;
            }
            
            if ("Todo".equals(entityName)) {
                return todoRepository.findByIdAndUserId(entityId, user.getId()).orElse(null);
            } else if ("Project".equals(entityName)) {
                return projectRepository.findByIdAndUser(entityId, user).orElse(null);
            }
        } catch (Exception e) {
            log.warn("변경 전 엔티티 조회 실패: entityName={}, entityId={}", entityName, entityId, e);
        }
        return null;
    }

    /**
     * 현재 인증된 사용자 정보 추출
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            }
        } catch (Exception e) {
            log.warn("사용자 정보 추출 실패: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 클라이언트 IP 주소 추출
     * X-Forwarded-For 헤더를 고려하여 프록시 환경에서도 정확한 IP를 추출합니다.
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes == null) {
                return null;
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // X-Forwarded-For 헤더 확인 (프록시 환경)
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                // 여러 IP가 있을 경우 첫 번째 IP 사용
                return xForwardedFor.split(",")[0].trim();
            }
            
            // X-Real-IP 헤더 확인
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            
            // 기본 IP 주소
            return request.getRemoteAddr();
            
        } catch (Exception e) {
            log.warn("IP 주소 추출 실패: {}", e.getMessage());
            return null;
        }
    }
}
