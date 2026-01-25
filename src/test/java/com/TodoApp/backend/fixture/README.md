# Test Fixture 사용 가이드

이 디렉토리는 확장 가능한 테스트 Fixture 구조를 제공합니다. **test-utils** 라이브러리(FixtureMonkey 기반)를 활용하여 테스트 데이터를 자동 생성합니다.

## 구조

```
fixture/
├── core/
│   ├── BaseFixture.java          # 모든 Fixture의 기본 추상 클래스 (test-utils 기반)
│   ├── FixtureMonkeyConfig.java  # 프로젝트 특화 FixtureMonkey 설정
│   ├── TestDataGraph.java         # 연관 관계 그래프 빌더 (test-utils 기반)
│   └── FixtureRegistry.java       # Fixture 중앙 관리
├── dto/
│   ├── SignupRequestValidationTest.java    # DTO 검증 테스트
│   ├── TodoRequestValidationTest.java      # DTO 검증 테스트
│   └── ProjectRequestValidationTest.java   # DTO 검증 테스트
├── UserFixture.java               # User 엔티티 Fixture
├── TodoFixture.java               # Todo 엔티티 Fixture
└── ProjectFixture.java            # Project 엔티티 Fixture
```

## test-utils 라이브러리

이 프로젝트는 `com.utils:test-utils` 라이브러리를 사용합니다:
- **FixtureMonkey**: 테스트 데이터 자동 생성
- **Jakarta Validation Plugin**: Bean Validation 어노테이션 기반 데이터 생성
- **Jqwik**: 속성 기반 테스트 지원

## 기본 사용법

## 기본 사용법

### 1. 단일 엔티티 생성

```java
// 기본 User 생성
User user = UserFixture.aUser();

// Admin User 생성
User admin = UserFixture.anAdmin();

// 커스텀 User 생성
User customUser = UserFixture.user().a(builder -> {
    builder.username("customuser");
    builder.email("custom@example.com");
    builder.role(User.Role.ADMIN);
});

// User와 연관된 Todo 생성
Todo todo = TodoFixture.aTodoFor(user);

// 완료된 Todo 생성
Todo completedTodo = TodoFixture.aCompletedTodoFor(user);
```

### 2. 여러 엔티티 생성

```java
// 5개의 User 생성
List<User> users = UserFixture.user().many(5);

// 10개의 Todo 생성 (커스터마이징)
List<Todo> todos = TodoFixture.todo().many(10, builder -> {
    builder.priority(Todo.Priority.HIGH);
});

// User와 연관된 여러 Todo 생성
List<Todo> userTodos = TodoFixture.todosFor(user, 5);
```

### 3. 연관 관계 그래프 생성

```java
// 복잡한 연관 관계를 포함한 테스트 데이터 그래프 생성
TestDataGraph graph = TestDataGraph.create()
        .withUser(UserFixture.aUser())
        .withMany(Todo.class, TodoFixture.todosFor(graph.getUser(), 10))
        .withMany(Project.class, ProjectFixture.projectsFor(graph.getUser(), 2));

// 연관 관계 자동 설정
graph.link(Todo.class, todo -> todo.setUser(graph.getUser()))
     .link(Project.class, project -> project.setUser(graph.getUser()));

// 특정 엔티티 조회
User user = graph.getUser();
List<Todo> todos = graph.get(Todo.class);
Project firstProject = graph.getFirst(Project.class);

// test-utils를 직접 사용하여 엔티티 생성 (새로운 방식)
TestDataGraph graph2 = TestDataGraph.create()
        .withUser(UserFixture.aUser())
        .withManyEntities(Todo.class, 10)  // test-utils로 자동 생성
        .withManyEntities(Project.class, 2);
```

### 4. 기존 테스트 코드 개선 예시

**Before:**
```java
@BeforeEach
void setUp() {
    testUser = User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("password")
            .role(User.Role.USER)
            .build();
    testUser.setId(1L);
    
    testTodo = Todo.builder()
            .user(testUser)
            .title("테스트 TODO")
            .status(Todo.TodoStatus.TODO)
            .build();
    testTodo.setId(1L);
}
```

**After:**
```java
@BeforeEach
void setUp() {
    testUser = UserFixture.aUser();
    testTodo = TodoFixture.aTodoFor(testUser);
}
```

### 4. DTO 검증 테스트 (Bean Validation 활용)

```java
// test-utils를 사용하여 DTO 생성 및 검증
TestSupport<SignupRequest> signupSupport = new TestSupport<>(SignupRequest.class);

// 유효한 DTO 생성
SignupRequest validRequest = signupSupport.monkey(r -> {
    r.setUsername("testuser");
    r.setEmail("test@example.com");
    r.setPassword("password123");
});

// Bean Validation 검증
Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
Set<ConstraintViolation<SignupRequest>> violations = validator.validate(validRequest);
assertThat(violations).isEmpty();
```

## 새로운 엔티티 추가하기

새로운 엔티티(예: Comment, Permission)를 추가할 때:

### 1. Fixture 클래스 생성 (test-utils 기반)

```java
package com.TodoApp.backend.fixture;

import com.TodoApp.backend.domain.comment.entity.Comment;
import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.fixture.core.BaseFixture;

public class CommentFixture extends BaseFixture<Comment, Comment.CommentBuilder> {
    
    private static final CommentFixture INSTANCE = new CommentFixture();
    
    private CommentFixture() {
        super(Comment.class);  // 엔티티 클래스 전달
    }
    
    public static CommentFixture comment() {
        return INSTANCE;
    }
    
    @Override
    protected Comment.CommentBuilder defaultBuilder() {
        // 하위 호환성을 위해 유지, 실제로는 test-utils가 사용됨
        return Comment.builder()
                .content("테스트 댓글 " + nextGlobalId());
    }
    
    @Override
    protected Comment buildFrom(Comment.CommentBuilder builder) {
        return builder.build();
    }
    
    @Override
    public Comment aDefault() {
        // test-utils를 사용하여 기본 Comment 생성
        Comment comment = getTestSupport().monkey(c -> {
            if (c.getContent() == null || c.getContent().isEmpty()) {
                c.setContent("테스트 댓글 " + nextGlobalId());
            }
        });
        comment.setId(nextId());
        return comment;
    }
    
    // 연관 관계를 포함한 생성 메서드
    public static Comment aCommentFor(Todo todo, User author) {
        Comment comment = comment().aDefault();
        comment.setTodo(todo);
        comment.setAuthor(author);
        return comment;
    }
}
```

### 2. FixtureRegistry에 등록 (선택적)

```java
// FixtureRegistry.java의 static 블록에 추가
register(Comment.class, CommentFixture.comment());
```

## test-utils 기반 마이그레이션

기존 Fixture 패턴에서 test-utils 기반으로 마이그레이션되었습니다:

### 변경 사항

1. **BaseFixture**: `TestSupport<T>` 기반으로 재구현
2. **Fixture 클래스들**: 내부적으로 FixtureMonkey를 사용하여 랜덤 데이터 생성
3. **Bean Validation 통합**: Jakarta Validation Plugin으로 유효한 테스트 데이터 생성
4. **DTO 검증 테스트**: 새로운 DTO 검증 테스트 추가

### 기존 API 호환성

기존 테스트 코드는 변경 없이 동작합니다:
- `UserFixture.aUser()` - 동일하게 사용 가능
- `TodoFixture.aTodoFor(user)` - 동일하게 사용 가능
- 내부 구현만 test-utils로 변경됨

## 장점

1. **일관성**: 모든 Fixture가 동일한 패턴을 따름
2. **재사용성**: 공통 기능을 BaseFixture에서 제공
3. **확장성**: 새 엔티티 추가 시 최소한의 코드로 확장 가능
4. **유지보수성**: 공통 로직 변경 시 한 곳만 수정
5. **타입 안전성**: 제네릭으로 컴파일 타임 타입 체크
6. **연관 관계 관리**: TestDataGraph로 복잡한 관계도 쉽게 구성
7. **랜덤 데이터 생성**: FixtureMonkey로 다양한 테스트 케이스 자동 생성
8. **Bean Validation 통합**: 유효한 테스트 데이터 자동 생성
9. **코드 중복 감소**: 각 엔티티별 Fixture 클래스 코드량 감소

## 참고 자료

- [FixtureMonkey 공식 문서](https://github.com/naver/fixture-monkey)
- [test-utils 프로젝트](../../../../test-utils/README.md)

