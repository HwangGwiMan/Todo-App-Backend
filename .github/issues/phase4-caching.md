# [Phase 4] 캐싱 전략 구현

## 개요

Spring Cache를 활용하여 자주 조회되는 데이터에 대한 캐싱 전략을 구현합니다.

## 우선순위
중간

## 예상 소요 시간
3-4시간

## 구현 내용

### 1. CacheConfig 설정
```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("todos"),
            new ConcurrentMapCache("projects"),
            new ConcurrentMapCache("stats")
        ));
        return cacheManager;
    }
}
```

### 2. Service에 캐싱 적용
```java
@Cacheable(value = "todos", key = "#userId + '_' + #todoId")
public TodoResponse getTodo(Long userId, Long todoId) {
    // 캐시 미스 시에만 실행
}

@CacheEvict(value = "todos", key = "#userId + '_' + #todoId")
public TodoResponse updateTodo(Long userId, Long todoId, TodoRequest request) {
    // 업데이트 후 캐시 삭제
}
```

## 주의사항
- 프로덕션에서는 Redis 사용 권장
- 캐시 TTL 설정 필요
- 캐시 일관성 보장

## 체크리스트
- [ ] @EnableCaching 설정
- [ ] CacheManager 빈 등록
- [ ] 주요 조회 메서드에 @Cacheable 적용
- [ ] 수정/삭제 메서드에 @CacheEvict 적용
- [ ] 캐시 키 전략 설계
- [ ] 캐시 모니터링 로그 추가
- [ ] 성능 테스트

## 향후 개선
- [ ] Redis 연동
- [ ] 캐시 TTL 설정
- [ ] 분산 캐시 전략

