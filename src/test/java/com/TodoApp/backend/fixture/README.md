# Test Fixture 사용 가이드

이 디렉토리는 확장 가능한 테스트 Fixture 구조를 제공합니다.

## 구조

```
fixture/
├── core/
│   ├── BaseFixture.java          # 모든 Fixture의 기본 추상 클래스
│   ├── TestDataGraph.java         # 연관 관계 그래프 빌더
│   └── FixtureRegistry.java       # Fixture 중앙 관리
├── UserFixture.java               # User 엔티티 Fixture
├── TodoFixture.java               # Todo 엔티티 Fixture
└── ProjectFixture.java            # Project 엔티티 Fixture
```

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

## 새로운 엔티티 추가하기

새로운 엔티티(예: Comment, Permission)를 추가할 때:

### 1. Fixture 클래스 생성

```java
package com.TodoApp.backend.fixture;

import com.TodoApp.backend.domain.comment.entity.Comment;
import com.TodoApp.backend.domain.todo.entity.Todo;
import com.TodoApp.backend.domain.user.entity.User;
import com.TodoApp.backend.fixture.core.BaseFixture;

public class CommentFixture extends BaseFixture<Comment, Comment.CommentBuilder> {
    
    private static final CommentFixture INSTANCE = new CommentFixture();
    
    public static CommentFixture comment() {
        return INSTANCE;
    }
    
    @Override
    protected Comment.CommentBuilder defaultBuilder() {
        return Comment.builder()
                .content("테스트 댓글 " + nextGlobalId());
    }
    
    @Override
    protected Comment buildFrom(Comment.CommentBuilder builder) {
        return builder.build();
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

## 장점

1. **일관성**: 모든 Fixture가 동일한 패턴을 따름
2. **재사용성**: 공통 기능을 BaseFixture에서 제공
3. **확장성**: 새 엔티티 추가 시 최소한의 코드로 확장 가능
4. **유지보수성**: 공통 로직 변경 시 한 곳만 수정
5. **타입 안전성**: 제네릭으로 컴파일 타임 타입 체크
6. **연관 관계 관리**: TestDataGraph로 복잡한 관계도 쉽게 구성

