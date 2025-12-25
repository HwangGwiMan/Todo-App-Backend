# [Phase 4] Rate Limiting 구현

## 개요

API 남용을 방지하기 위한 Rate Limiting을 구현합니다.

## 우선순위
낮음 (선택사항)

## 예상 소요 시간
2-3시간

## 구현 내용

### 1. Google Guava RateLimiter 사용
```gradle
implementation 'com.google.guava:guava:32.1.3-jre'
```

### 2. RateLimitAspect 구현
```java
@Aspect
@Component
public class RateLimitAspect {
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();
    
    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) 
        throws Throwable {
        
        String key = getCurrentUserKey();
        RateLimiter limiter = limiters.computeIfAbsent(
            key, 
            k -> RateLimiter.create(rateLimit.permitsPerSecond())
        );
        
        if (!limiter.tryAcquire()) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }
        
        return joinPoint.proceed();
    }
}
```

### 3. 사용 예시
```java
@RateLimit(permitsPerSecond = 10.0)
@PostMapping
public ResponseEntity<?> createTodo(@RequestBody TodoRequest request) {
    // 초당 10개 요청 제한
}
```

## 체크리스트
- [ ] Guava 의존성 추가
- [ ] @RateLimit 어노테이션 정의
- [ ] RateLimitAspect 구현
- [ ] Controller에 적용
- [ ] ErrorCode 추가 (TOO_MANY_REQUESTS)
- [ ] 테스트 코드 작성

## 향후 개선
- [ ] Redis 기반 분산 Rate Limiting
- [ ] API Key별 제한
- [ ] 사용량 모니터링 대시보드

