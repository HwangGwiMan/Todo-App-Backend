# [Phase 4] Spring Events로 관심사 분리

## 개요

Spring Events를 활용하여 Service 계층의 관심사를 분리하고, 비동기 처리가 가능한 구조로 개선합니다.

## 우선순위
중간

## 예상 소요 시간
3-4시간

## 구현 내용

### 1. 이벤트 클래스 정의
```java
@Getter
@AllArgsConstructor
public class TodoCreatedEvent {
    private final Todo todo;
    private final User user;
}
```

### 2. Service에서 이벤트 발행
```java
@Transactional
public TodoResponse createTodo(Long userId, TodoRequest request) {
    Todo saved = todoRepository.save(todo);
    eventPublisher.publishEvent(new TodoCreatedEvent(saved, user));
    return TodoResponse.from(saved);
}
```

### 3. 이벤트 리스너 구현
```java
@Component
@RequiredArgsConstructor
public class TodoEventListener {
    @Async
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoCreated(TodoCreatedEvent event) {
        log.info("TODO 생성됨: {}", event.getTodo().getTitle());
    }
}
```

## 장점
- Service 계층의 책임 분리
- 비동기 처리 가능
- 기능 추가 시 기존 코드 수정 불필요

## 체크리스트
- [x] 이벤트 클래스 정의
  - [x] TodoCreatedEvent
  - [x] TodoUpdatedEvent
  - [x] TodoDeletedEvent
  - [x] ProjectCreatedEvent
- [x] @EnableAsync 설정
- [x] 이벤트 리스너 구현
- [x] Service에서 이벤트 발행 추가
- [x] 테스트 코드 작성

