# [Phase 4] Soft Delete 구현

## 개요

물리적 삭제 대신 논리적 삭제를 구현하여 데이터 복구 기능을 제공합니다.

## 우선순위
낮음 (선택사항)

## 예상 소요 시간
2-3시간

## 구현 내용

### 1. 엔티티에 deletedAt 필드 추가
```java
@Entity
@SQLDelete(sql = "UPDATE todos SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Todo extends BaseEntity {
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
```

### 2. 복구 API 추가
```java
@Transactional
public void restoreTodo(Long todoId, Long userId) {
    Todo todo = todoRepository.findByIdIncludingDeleted(todoId)
        .orElseThrow(() -> new BusinessException(ErrorCode.TODO_NOT_FOUND));
    
    if (!todo.getUser().getId().equals(userId)) {
        throw new BusinessException(ErrorCode.TODO_ACCESS_DENIED);
    }
    
    todo.setDeletedAt(null);
    todoRepository.save(todo);
}
```

### 3. 휴지통 기능
- 삭제된 항목 조회 API
- 복구 API
- 영구 삭제 API

## 체크리스트
- [ ] 엔티티에 deletedAt 필드 추가
- [ ] @SQLDelete, @Where 어노테이션 적용
- [ ] 복구 API 구현
- [ ] 휴지통 조회 API
- [ ] 영구 삭제 API (관리자용)
- [ ] 자동 삭제 스케줄러 (선택)
- [ ] 테스트 코드 작성

## 향후 개선
- [ ] 30일 후 자동 영구 삭제
- [ ] 삭제된 항목 통계
- [ ] 대량 복구 기능

