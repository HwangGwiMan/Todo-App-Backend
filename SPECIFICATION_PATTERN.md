# Specification 패턴 구현 가이드

## 📋 문제 상황

### Before (if-else 체인 문제)

`TodoService.getTodos()` 메서드에서 검색 조건에 따라 다른 Repository 메서드를 호출:

```java
// 기존 코드 (약 40줄)
public Page<TodoResponse> getTodos(Long userId, TodoSearchRequest searchRequest) {
    Pageable pageable = createPageable(searchRequest);
    Page<Todo> todos;

    // 복잡한 if-else 체인 ❌
    if (searchRequest.getProjectId() != null) {
        todos = todoRepository.findByUserAndProjectId(user, searchRequest.getProjectId(), pageable);
    } else {
        if (keyword != null && !keyword.isEmpty()) {
            todos = todoRepository.searchByKeyword(userId, keyword, pageable);
        } else if (searchRequest.getStatus() != null) {
            todos = todoRepository.findByUserIdAndStatus(userId, searchRequest.getStatus(), pageable);
        } else if (searchRequest.getPriority() != null) {
            todos = todoRepository.findByUserIdAndPriority(userId, searchRequest.getPriority(), pageable);
        } else if (searchRequest.getDueDateStart() != null && searchRequest.getDueDateEnd() != null) {
            todos = todoRepository.findByUserIdAndDueDateBetween(...);
        } else {
            todos = todoRepository.findByUserId(userId, pageable);
        }
    }
    
    return todos.map(TodoResponse::from);
}
```

**문제점:**
- ❌ **복합 필터 조합 불가능**: 키워드 + 상태 + 우선순위를 동시에 필터링할 수 없음
- ❌ **코드 복잡도 증가**: 새로운 필터 추가 시 if-else 체인 계속 증가
- ❌ **가독성 저하**: 비즈니스 로직이 조건문에 묻혀버림
- ❌ **유지보수 어려움**: 조건 변경 시 여러 곳 수정 필요
- ❌ **Repository 메서드 폭발**: 각 조건 조합마다 메서드 필요

---

## ✅ 해결 방법: Specification 패턴

### 1단계: TodoSpecification 클래스 생성

```java
// TodoSpecification.java
public class TodoSpecification {
    
    /**
     * 사용자 ID 필터
     */
    public static Specification<Todo> hasUserId(Long userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null) {
                return criteriaBuilder.conjunction(); // true 반환
            }
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }
    
    /**
     * 키워드 검색 (제목 또는 설명)
     */
    public static Specification<Todo> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String likePattern = "%" + keyword.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), 
                    likePattern
                ),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), 
                    likePattern
                )
            );
        };
    }
    
    /**
     * 상태 필터
     */
    public static Specification<Todo> hasStatus(Todo.TodoStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }
    
    // 우선순위, 프로젝트 ID, 마감일 범위 등 다른 필터들도 동일한 패턴으로 구현
}
```

**핵심 개념:**
- `Specification<T>`: JPA Criteria API를 감싸는 인터페이스
- `null` 체크 후 `conjunction()` 반환 → 조건 무시
- 각 필터가 독립적으로 동작

### 2단계: TodoRepository에 JpaSpecificationExecutor 추가

```java
// TodoRepository.java
@Repository
public interface TodoRepository extends JpaRepository<Todo, Long>, 
                                        JpaSpecificationExecutor<Todo>,  // ✅ 추가
                                        TodoRepositoryCustom {
    // 기존 메서드들 유지
}
```

**JpaSpecificationExecutor가 제공하는 메서드:**
- `findAll(Specification<T> spec)`
- `findAll(Specification<T> spec, Pageable pageable)`
- `findAll(Specification<T> spec, Sort sort)`
- `count(Specification<T> spec)`

### 3단계: TodoService 리팩토링

```java
// After (Specification 패턴) ✅
public Page<TodoResponse> getTodos(Long userId, TodoSearchRequest searchRequest) {
    // 사용자 존재 확인
    userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // Specification 조합 (복합 필터 지원!) ✨
    Specification<Todo> spec = TodoSpecification.hasUserId(userId)
            .and(TodoSpecification.hasKeyword(searchRequest.getKeyword()))
            .and(TodoSpecification.hasStatus(searchRequest.getStatus()))
            .and(TodoSpecification.hasPriority(searchRequest.getPriority()))
            .and(TodoSpecification.hasProjectId(searchRequest.getProjectId()))
            .and(TodoSpecification.dueDateBetween(
                    searchRequest.getDueDateStart(),
                    searchRequest.getDueDateEnd()
            ));

    // Pageable 생성 (정렬 포함)
    Pageable pageable = createPageable(searchRequest);

    // Specification을 사용한 동적 쿼리 실행
    Page<Todo> todos = todoRepository.findAll(spec, pageable);

    return todos.map(TodoResponse::from);
}
```

**실행되는 SQL:**
```sql
SELECT *
FROM todos t
INNER JOIN users u ON t.user_id = u.id
WHERE u.id = ?                                      -- hasUserId
  AND (LOWER(t.title) LIKE ? OR LOWER(t.description) LIKE ?)  -- hasKeyword
  AND t.status = ?                                  -- hasStatus
  AND t.priority = ?                                -- hasPriority
  AND t.project_id = ?                              -- hasProjectId
  AND t.due_date BETWEEN ? AND ?                    -- dueDateBetween
ORDER BY t.created_at DESC
LIMIT ? OFFSET ?
```

**단 하나의 쿼리로 모든 조건 처리!** ✅

---

## 📊 개선 효과

### 1. 코드 간결성

| 항목 | Before | After | 개선 |
|-----|--------|-------|------|
| 코드 줄 수 | ~40줄 | ~15줄 | **62% ↓** |
| if-else 분기 | 6개 | 0개 | **100% 제거** |
| 복잡도 (Cyclomatic) | 7 | 1 | **86% ↓** |

### 2. 복합 필터 조합

**Before:** 단일 조건만 가능
```
❌ 키워드 검색만
❌ 상태 필터만
❌ 우선순위 필터만
```

**After:** 모든 조건 자유롭게 조합 가능
```
✅ 키워드 + 상태
✅ 키워드 + 상태 + 우선순위
✅ 키워드 + 상태 + 우선순위 + 프로젝트
✅ 상태 + 마감일 범위
✅ 모든 조건 동시 적용
```

### 3. 확장성

**새로운 필터 추가 시:**

Before:
```java
// 1. Repository에 새 메서드 추가
Page<Todo> findByUserIdAndLabel(Long userId, String label, Pageable pageable);

// 2. Service에 새 else-if 분기 추가
else if (searchRequest.getLabel() != null) {
    todos = todoRepository.findByUserIdAndLabel(userId, searchRequest.getLabel(), pageable);
}
```

After:
```java
// 1. Specification에 새 메서드 추가만!
public static Specification<Todo> hasLabel(String label) {
    return (root, query, cb) -> 
        label != null ? cb.equal(root.get("label"), label) : cb.conjunction();
}

// 2. Service에서 자동으로 조합됨 (코드 수정 불필요!)
.and(TodoSpecification.hasLabel(searchRequest.getLabel()))
```

---

## 🎯 주요 장점

### 1. **타입 안전성**
```java
// Criteria API의 타입 안전한 쿼리 생성
criteriaBuilder.equal(root.get("status"), status)  // 컴파일 타임 검증
```

### 2. **재사용성**
```java
// 다른 곳에서도 동일한 Specification 재사용 가능
Specification<Todo> highPriorityTodos = TodoSpecification.hasPriority(Priority.HIGH);
Specification<Todo> overdueTodos = TodoSpecification.dueDateBefore(LocalDateTime.now());

// 조합하여 새로운 조건 생성
Specification<Todo> urgentTodos = highPriorityTodos.and(overdueTodos);
```

### 3. **테스트 용이성**
```java
@Test
void testHasKeywordSpecification() {
    Specification<Todo> spec = TodoSpecification.hasKeyword("Spring");
    List<Todo> result = todoRepository.findAll(spec);
    
    assertThat(result).allMatch(todo -> 
        todo.getTitle().contains("Spring") || 
        todo.getDescription().contains("Spring")
    );
}
```

### 4. **복잡한 조건도 간단히**
```java
// OR 조건
Specification<Todo> spec = TodoSpecification.hasStatus(TodoStatus.TODO)
    .or(TodoSpecification.hasStatus(TodoStatus.IN_PROGRESS));

// NOT 조건
Specification<Todo> spec = Specification.not(TodoSpecification.isCompleted());

// 복합 조건
Specification<Todo> urgentAndNotCompleted = 
    TodoSpecification.hasPriority(Priority.HIGH)
        .and(TodoSpecification.isNotCompleted())
        .and(TodoSpecification.dueDateBefore(LocalDateTime.now().plusDays(3)));
```

---

## 🧪 테스트 방법

### 1. 복합 필터 테스트

```bash
# 키워드 + 상태 + 우선순위 동시 필터링
GET /api/todos?keyword=Spring&status=TODO&priority=HIGH&page=0&size=20
```

**Before:** 키워드만 검색됨 (나머지 무시)  
**After:** 모든 조건 동시 적용 ✅

### 2. 쿼리 로그 확인

`application-dev.yml`:
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

**확인할 내용:**
- WHERE 절에 모든 조건이 AND로 결합되어 있는지
- 단일 쿼리로 실행되는지

---

## 💡 추가 활용 예시

### 1. 대시보드용 복잡한 조건

```java
// 긴급 TODO: 우선순위 HIGH + 마감일 3일 이내 + 미완료
public List<TodoResponse> getUrgentTodos(Long userId) {
    Specification<Todo> spec = TodoSpecification.hasUserId(userId)
            .and(TodoSpecification.hasPriority(Priority.HIGH))
            .and(TodoSpecification.dueDateBefore(LocalDateTime.now().plusDays(3)))
            .and(TodoSpecification.isNotCompleted());
    
    return todoRepository.findAll(spec).stream()
            .map(TodoResponse::from)
            .collect(Collectors.toList());
}
```

### 2. 통계용 동적 카운트

```java
// 특정 조건의 TODO 개수
public long countTodosByCondition(Long userId, TodoSearchRequest request) {
    Specification<Todo> spec = buildSpecification(userId, request);
    return todoRepository.count(spec);
}
```

### 3. 저장된 검색 조건

```java
// 사용자가 자주 사용하는 필터 조합을 저장하고 재사용
@Entity
class SavedSearch {
    private String name;
    private TodoStatus status;
    private Priority priority;
    // ...
    
    public Specification<Todo> toSpecification(Long userId) {
        return TodoSpecification.hasUserId(userId)
                .and(TodoSpecification.hasStatus(status))
                .and(TodoSpecification.hasPriority(priority));
    }
}
```

---

## 📚 참고 자료

- [Spring Data JPA Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications)
- [JPA Criteria API](https://docs.oracle.com/javaee/7/tutorial/persistence-criteria.htm)
- [Specification Pattern](https://en.wikipedia.org/wiki/Specification_pattern)
- [Baeldung - Spring Data JPA Specifications](https://www.baeldung.com/rest-api-search-language-spring-data-specifications)

---

## ✅ 체크리스트

- [x] `TodoSpecification` 클래스 생성
- [x] 모든 필터 조건을 Specification으로 구현
  - [x] hasUserId
  - [x] hasKeyword
  - [x] hasStatus
  - [x] hasPriority
  - [x] hasProjectId
  - [x] dueDateBetween
  - [x] isCompleted / isNotCompleted
- [x] `TodoRepository`에 `JpaSpecificationExecutor` 추가
- [x] `TodoService.getTodos` 리팩토링
- [x] LocalDateTime 타입 호환성 수정
- [x] deprecated 메서드 수정
- [ ] 복합 필터 API 테스트 (수동)
- [ ] 성능 벤치마크 (선택)
- [ ] README 업데이트

---

**작성일:** 2025-12-17  
**작성자:** Phase 4 - Specification 패턴 구현 프로젝트
