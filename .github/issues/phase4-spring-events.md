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
// TODO 이벤트
@Getter
@AllArgsConstructor
public class TodoCreatedEvent {
    private final Todo todo;
    private final User user;
}

@Getter
@AllArgsConstructor
public class TodoUpdatedEvent {
    private final Todo todo;
    private final User user;
}

@Getter
@AllArgsConstructor
public class TodoDeletedEvent {
    private final Todo todo;
    private final User user;
}

// Project 이벤트
@Getter
@AllArgsConstructor
public class ProjectCreatedEvent {
    private final Project project;
    private final User user;
}

@Getter
@AllArgsConstructor
public class ProjectUpdatedEvent {
    private final Project project;
    private final User user;
}

@Getter
@AllArgsConstructor
public class ProjectDeletedEvent {
    private final Project project;
    private final User user;
}
```

### 2. Service에서 이벤트 발행
```java
// TodoService
@Transactional
public TodoResponse createTodo(Long userId, TodoRequest request) {
    // ... 비즈니스 로직 ...
    Todo saved = todoRepository.save(todo);
    eventPublisher.publishEvent(new TodoCreatedEvent(saved, user));
    return todoMapper.toDto(saved);
}

@Transactional
public TodoResponse updateTodo(Long userId, Long todoId, TodoRequest request) {
    // ... 비즈니스 로직 ...
    Todo updatedTodo = todoRepository.save(todo);
    eventPublisher.publishEvent(new TodoUpdatedEvent(updatedTodo, user));
    return todoMapper.toDto(updatedTodo);
}

@Transactional
public void deleteTodo(Long userId, Long todoId) {
    // ... 비즈니스 로직 ...
    eventPublisher.publishEvent(new TodoDeletedEvent(todo, user));
    todoRepository.delete(todo);
}

// ProjectService
@Transactional
public ProjectResponse createProject(ProjectRequest request, User user) {
    // ... 비즈니스 로직 ...
    Project savedProject = projectRepository.save(project);
    eventPublisher.publishEvent(new ProjectCreatedEvent(savedProject, user));
    return projectMapper.toDtoWithCount(savedProject, 0L);
}

@Transactional
public ProjectResponse updateProject(Long projectId, ProjectRequest request, User user) {
    // ... 비즈니스 로직 ...
    Project updatedProject = projectRepository.save(project);
    eventPublisher.publishEvent(new ProjectUpdatedEvent(updatedProject, user));
    return projectMapper.toDtoWithCount(updatedProject, todoCount);
}

@Transactional
public void deleteProject(Long projectId, User user) {
    // ... 비즈니스 로직 ...
    eventPublisher.publishEvent(new ProjectDeletedEvent(project, user));
    projectRepository.delete(project);
}
```

### 3. 이벤트 리스너 구현
```java
// TodoEventListener
@Component
@RequiredArgsConstructor
@Slf4j
public class TodoEventListener {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoCreated(TodoCreatedEvent event) {
        log.info("TODO 생성됨: todoId={}, title={}, userId={}",
                event.getTodo().getId(), event.getTodo().getTitle(), event.getUser().getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoUpdated(TodoUpdatedEvent event) {
        log.info("TODO 수정됨: todoId={}, title={}, userId={}",
                event.getTodo().getId(), event.getTodo().getTitle(), event.getUser().getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTodoDeleted(TodoDeletedEvent event) {
        log.info("TODO 삭제됨: todoId={}, title={}, userId={}",
                event.getTodo().getId(), event.getTodo().getTitle(), event.getUser().getId());
    }
}

// ProjectEventListener
@Component
@RequiredArgsConstructor
@Slf4j
public class ProjectEventListener {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectCreated(ProjectCreatedEvent event) {
        log.info("프로젝트 생성됨: projectId={}, name={}, userId={}",
                event.getProject().getId(), event.getProject().getName(), event.getUser().getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectUpdated(ProjectUpdatedEvent event) {
        log.info("프로젝트 수정됨: projectId={}, name={}, userId={}",
                event.getProject().getId(), event.getProject().getName(), event.getUser().getId());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProjectDeleted(ProjectDeletedEvent event) {
        log.info("프로젝트 삭제됨: projectId={}, name={}, userId={}",
                event.getProject().getId(), event.getProject().getName(), event.getUser().getId());
    }
}
```

## 장점
- Service 계층의 책임 분리
- 비동기 처리 가능
- 기능 추가 시 기존 코드 수정 불필요

## 체크리스트
- [x] 이벤트 클래스 정의
  - [x] TodoCreatedEvent, TodoUpdatedEvent, TodoDeletedEvent
  - [x] ProjectCreatedEvent, ProjectUpdatedEvent, ProjectDeletedEvent
- [x] @EnableAsync 설정 (AsyncConfig.java에 이미 존재)
- [x] 이벤트 리스너 구현
  - [x] TodoEventListener (모든 TODO 이벤트 처리)
  - [x] ProjectEventListener (모든 Project 이벤트 처리)
- [x] Service에서 이벤트 발행 추가
  - [x] TodoService: createTodo, updateTodo, updateTodoStatus, deleteTodo
  - [x] ProjectService: createProject, updateProject, deleteProject
- [x] 빌드 및 테스트 실행 (성공)
- [ ] 단위 테스트 코드 작성 (향후 개선)

