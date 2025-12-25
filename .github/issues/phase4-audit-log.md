# [Phase 4] 감사 로그 시스템

## 개요

AOP를 활용하여 엔티티 변경 이력을 자동으로 기록하는 감사 로그 시스템을 구현합니다.

## 우선순위
낮음 (선택사항)

## 예상 소요 시간
5-6시간

## 구현 내용

### 1. AuditLog 엔티티 생성
```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    
    private String entityName;  // "Todo", "Project"
    private Long entityId;
    private String action;      // "CREATE", "UPDATE", "DELETE"
    private Long userId;
    private String username;
    
    @Column(columnDefinition = "TEXT")
    private String changesBefore;  // JSON
    
    @Column(columnDefinition = "TEXT")
    private String changesAfter;   // JSON
    
    private LocalDateTime timestamp;
    private String ipAddress;
}
```

### 2. AOP로 자동 감사
```java
@Aspect
@Component
public class AuditAspect {
    @AfterReturning(
        pointcut = "@annotation(auditable)",
        returning = "result"
    )
    public void logAudit(JoinPoint joinPoint, Auditable auditable, Object result) {
        // 감사 로그 기록
    }
}
```

### 3. @Auditable 어노테이션
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String action();
}
```

## 체크리스트
- [ ] AuditLog 엔티티 생성
- [ ] @Auditable 어노테이션 정의
- [ ] AuditAspect 구현
- [ ] Service 메서드에 @Auditable 적용
- [ ] 감사 로그 조회 API
- [ ] IP 주소 추적 기능
- [ ] 테스트 코드 작성

## 향후 확장
- [ ] 변경 전/후 비교 기능
- [ ] 감사 로그 검색 API
- [ ] 변경 이력 복구 기능

