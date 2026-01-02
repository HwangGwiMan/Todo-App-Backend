# TodoApp Backend

Spring Boot 3.5.7 + Java 17로 구축된 TodoApp 백엔드 API 서버입니다.

## 📋 프로젝트 정보

이 프로젝트는 독립적인 Git 레포지토리로 관리됩니다. 프론트엔드와 별도로 버전 관리됩니다.

## 📊 현재 개발 상태

- ✅ **Phase 1 완료**: 인증 시스템, TODO CRUD, 검색/필터링/정렬, 페이징, 통계 API, API 문서화
- ✅ **Phase 2 완료**: 프로젝트 관리, 프로젝트-TODO 연동, 기본 프로젝트 관리, 순서(position) 관리
- 🚧 **Phase 3 예정**: 고급 검색 기능, TODO 복제/템플릿, 태그 시스템, 성능 최적화, 보안 강화

## 🚀 시작하기

### 사전 요구사항

- Java 17+
- Gradle 8.x
- MariaDB 10.x 이상
- IDE (IntelliJ IDEA, Eclipse 등)

### 데이터베이스 설정

1. MariaDB 설치 및 실행
2. 데이터베이스 생성:
```sql
CREATE DATABASE todoapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. `src/main/resources/application.properties` 파일에서 데이터베이스 연결 정보 수정:
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/todoapp
spring.datasource.username=root
spring.datasource.password=your_password
```

### 설치 및 실행

```bash
# 프로젝트 루트 디렉토리에서

# Gradle Wrapper를 사용한 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# 또는 IDE에서 BackendApplication.java 실행
```

서버가 시작되면:
- API 서버: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## 📁 프로젝트 구조

```
src/main/java/com/TodoApp/backend/
├── BackendApplication.java          # Spring Boot 메인 클래스
│
├── domain/                          # 도메인별 패키지
│   ├── auth/                        # 인증 도메인
│   │   ├── controller/
│   │   │   └── AuthController.java  # 인증 API 컨트롤러
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── SignupRequest.java
│   │   │   └── AuthResponse.java
│   │   ├── filter/
│   │   │   └── JwtAuthenticationFilter.java  # JWT 인증 필터
│   │   └── service/
│   │       ├── AuthService.java
│   │       └── CustomUserDetailsService.java
│   │
│   ├── project/                     # 프로젝트 도메인 ✅
│   │   ├── controller/
│   │   │   └── ProjectController.java  # 프로젝트 API 컨트롤러
│   │   ├── dto/
│   │   │   ├── ProjectRequest.java
│   │   │   └── ProjectResponse.java
│   │   ├── entity/
│   │   │   └── Project.java        # 프로젝트 엔티티
│   │   ├── repository/
│   │   │   └── ProjectRepository.java  # JPA 리포지토리
│   │   └── service/
│   │       └── ProjectService.java
│   │
│   ├── todo/                        # TODO 도메인
│   │   ├── controller/
│   │   │   └── TodoController.java  # TODO API 컨트롤러
│   │   ├── dto/
│   │   │   ├── TodoRequest.java
│   │   │   ├── TodoResponse.java
│   │   │   └── TodoSearchRequest.java
│   │   ├── entity/
│   │   │   └── Todo.java           # TODO 엔티티
│   │   ├── repository/
│   │   │   └── TodoRepository.java  # JPA 리포지토리
│   │   └── service/
│   │       └── TodoService.java
│   │
│   └── user/                        # 사용자 도메인
│       ├── entity/
│       │   └── User.java            # 사용자 엔티티
│       └── repository/
│           └── UserRepository.java
│
└── global/                          # 전역 설정 및 공통 코드
    ├── common/
    │   └── dto/
    │       ├── ApiResponse.java     # 공통 API 응답 래퍼
    │       ├── ErrorResponse.java   # 에러 응답 DTO ✅
    │       └── MessageResponse.java
    ├── config/
    │   ├── OpenApiConfig.java       # Swagger/OpenAPI 설정
    │   └── SecurityConfig.java     # Spring Security 설정
    ├── exception/
    │   ├── BusinessException.java   # 비즈니스 예외 클래스 ✅
    │   ├── ErrorCode.java          # 에러 코드 enum ✅
    │   └── GlobalExceptionHandler.java  # 전역 예외 처리 ✅
    └── security/
        └── JwtUtil.java            # JWT 유틸리티
```

## 🔐 인증 및 보안

### JWT 토큰 기반 인증

이 프로젝트는 JWT (JSON Web Token)를 사용한 인증 방식을 사용합니다.

#### 인증 흐름

1. **회원가입** (`POST /api/auth/signup`)
   - 사용자 정보를 받아 계정 생성
   - 자동으로 로그인 처리하여 JWT 토큰 발급

2. **로그인** (`POST /api/auth/login`)
   - 사용자명과 비밀번호로 인증
   - 성공 시 JWT 토큰 발급

3. **인증이 필요한 API 호출**
   - `Authorization: Bearer {token}` 헤더에 JWT 토큰 포함
   - `JwtAuthenticationFilter`가 토큰 검증 및 사용자 정보 추출

#### 공개 엔드포인트

인증 없이 접근 가능한 엔드포인트:
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인

#### 보호된 엔드포인트

모든 `/api/todos/**` 및 `/api/projects/**` 엔드포인트는 인증이 필요합니다.

### JWT 설정

`application.properties`에서 JWT 설정:
```properties
# JWT Secret Key (프로덕션에서는 반드시 변경!)
jwt.secret=your-secret-key-must-be-at-least-256-bits-long-for-HS256-algorithm-please-change-this-in-production
jwt.expiration=86400000  # 24시간 (밀리초)
```

## 📡 API 엔드포인트

### 인증 API

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| POST | `/api/auth/signup` | 회원가입 | ❌ |
| POST | `/api/auth/login` | 로그인 | ❌ |
| GET | `/api/auth/test` | 인증 테스트 | ✅ |

### TODO API

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| GET | `/api/todos` | TODO 목록 조회 (검색, 필터링, 페이징) | ✅ |
| GET | `/api/todos/{todoId}` | TODO 상세 조회 | ✅ |
| POST | `/api/todos` | TODO 생성 | ✅ |
| PUT | `/api/todos/{todoId}` | TODO 수정 | ✅ |
| PATCH | `/api/todos/{todoId}/status` | TODO 상태 변경 | ✅ |
| DELETE | `/api/todos/{todoId}` | TODO 삭제 | ✅ |
| GET | `/api/todos/stats` | 사용자 통계 조회 | ✅ |

### 프로젝트 API ✅

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| GET | `/api/projects` | 프로젝트 목록 조회 | ✅ |
| GET | `/api/projects/{projectId}` | 프로젝트 상세 조회 | ✅ |
| POST | `/api/projects` | 프로젝트 생성 | ✅ |
| PUT | `/api/projects/{projectId}` | 프로젝트 수정 | ✅ |
| DELETE | `/api/projects/{projectId}` | 프로젝트 삭제 | ✅ |
| GET | `/api/projects/default` | 기본 프로젝트 조회 | ✅ |

### API 응답 형식

모든 API는 공통 응답 형식을 사용합니다:

```json
{
  "success": true,
  "message": "성공 메시지",
  "data": {
    // 응답 데이터
  }
}
```

에러 응답:
```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null
}
```

### 검색 및 필터링

`GET /api/todos` 엔드포인트는 다음 쿼리 파라미터를 지원합니다:

- `keyword`: 검색 키워드 (제목, 설명)
- `projectId`: 프로젝트 ID 필터 ✅
- `dueDateStart`: 마감일 시작 범위
- `dueDateEnd`: 마감일 종료 범위
- `status`: 상태 필터 (TODO, IN_PROGRESS, DONE)
- `priority`: 우선순위 필터 (HIGH, MEDIUM, LOW)
- `sortBy`: 정렬 필드 (createdAt, dueDate, priority, position, title)
- `sortDirection`: 정렬 방향 (ASC, DESC)
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기

예시:
```
# 프로젝트별 TODO 조회
GET /api/todos?projectId=1&status=TODO&priority=HIGH&sortBy=createdAt&sortDirection=DESC&page=0&size=20

# 날짜 범위 검색
GET /api/todos?dueDateStart=2025-01-01T00:00:00&dueDateEnd=2025-01-31T23:59:59

# 키워드 검색
GET /api/todos?keyword=회의&page=0&size=10
```

**참고**: Spring의 `@ModelAttribute`는 평면 쿼리 파라미터를 기대합니다. 프론트엔드에서 중첩 객체(`searchRequest[page]=0`) 형식이 아닌 평면 형식(`page=0`)으로 전달해야 합니다.

## 🎯 개발 진행 상황

### ✅ Phase 1 완료 (2025년 11월)

**구현 완료된 기능:**

- [x] **인증 시스템**
  - JWT 기반 인증 (jjwt 0.12.3)
  - 회원가입/로그인 API (`POST /api/auth/signup`, `POST /api/auth/login`)
  - JWT 인증 필터 (`JwtAuthenticationFilter`)
  - Spring Security 통합
  - 사용자 정보 관리 (User 엔티티, UserRepository)

- [x] **TODO CRUD API**
  - TODO 생성 (`POST /api/todos`)
  - TODO 조회 (`GET /api/todos`, `GET /api/todos/{id}`)
  - TODO 수정 (`PUT /api/todos/{id}`)
  - TODO 상태 변경 (`PATCH /api/todos/{id}/status`)
  - TODO 삭제 (`DELETE /api/todos/{id}`)
  - 사용자별 권한 검증 (자신의 TODO만 접근 가능)

- [x] **검색 및 필터링**
  - 키워드 검색 (제목, 설명 LIKE 검색)
  - 상태 필터링 (TODO, IN_PROGRESS, DONE)
  - 우선순위 필터링 (HIGH, MEDIUM, LOW)
  - 프로젝트 ID 필터링 (Phase 2 통합)
  - 정렬 기능 (생성일, 마감일, 우선순위, position, 제목)
  - 정렬 방향 (ASC, DESC)
  - 페이징 지원 (Spring Data Pageable)

- [x] **통계 API**
  - 사용자별 TODO 통계 (`GET /api/todos/stats`)
  - 전체, 할 일, 진행중, 완료 개수
  - 완료율 계산
  - 지난 마감일 TODO 개수

- [x] **API 문서화**
  - OpenAPI/Swagger 통합 (SpringDoc OpenAPI 2.8.9)
  - Swagger UI 제공 (`/swagger-ui.html`)
  - OpenAPI JSON 스펙 (`/api-docs`)
  - API 스펙 자동 생성 및 프론트엔드 연동

- [x] **예외 처리** ✅
  - 전역 예외 핸들러 (`GlobalExceptionHandler`)
  - 커스텀 예외 체계 (`BusinessException`, `ErrorCode`)
  - 공통 에러 응답 형식 (`ErrorResponse`, `ApiResponse`)
  - Bean Validation 유효성 검사
  - 사용자 친화적 에러 메시지
  - 도메인별 에러 코드 관리
  - 자동 로깅 및 모니터링

- [x] **데이터베이스**
  - MariaDB 연동
  - JPA/Hibernate 사용
  - 엔티티 관계 설정 (User-Todo, User-Project)
  - BaseEntity 공통 필드 (createdAt, updatedAt)
  - NULL 안전성 보장

- [x] **테스트**
  - 단위 테스트 (TodoServiceTest, ProjectServiceTest, AuthServiceTest)
  - 테스트 Fixture 시스템 (TodoFixture, ProjectFixture, UserFixture)

### ✅ Phase 2 완료 (2025년 11월)

**구현 완료된 기능:**

- [x] **프로젝트 관리**
  - 프로젝트 엔티티 및 CRUD API
    - 프로젝트 목록 조회 (`GET /api/projects`)
    - 프로젝트 상세 조회 (`GET /api/projects/{id}`)
    - 프로젝트 생성 (`POST /api/projects`)
    - 프로젝트 수정 (`PUT /api/projects/{id}`)
    - 프로젝트 삭제 (`DELETE /api/projects/{id}`)
  - 프로젝트별 TODO 그룹화 (`projectId` 필터)
  - 기본 프로젝트 관리 (`GET /api/projects/default`)
  - 프로젝트 색상 및 순서(position) 관리
  - 프로젝트명 중복 검증
  - 프로젝트 삭제 시 관련 TODO 처리 (projectId NULL 처리)

- [x] **확장된 검색 및 필터링**
  - 프로젝트 ID 필터링 지원
  - TODO-프로젝트 연관 관계 구현
  - 날짜 범위 검색 (dueDateStart, dueDateEnd) ✅

- [x] **순서 관리**
  - TODO position 필드 지원
  - 프로젝트 position 필드 및 자동 정렬
  - position 기반 정렬 기능

- [x] **데이터 무결성**
  - 프로젝트-TODO 관계 설정
  - 기본 프로젝트 관리 로직 (단일 기본 프로젝트 보장)
  - CASCADE 처리 및 NULL 안전성
  - 사용자별 데이터 격리

### 🚧 Phase 3 진행 중 / 예정

**현재 상태:**
- [x] 날짜 범위 검색 구현 완료 (dueDateStart, dueDateEnd)
- [x] position 필드 구현 완료 (순서 관리)
- [ ] TODO 순서 변경 전용 API (현재는 PUT으로 position 업데이트 가능)

**다음 단계 구현 예정:**

- [ ] **TODO 고급 기능**
  - TODO 순서 변경 전용 API (`PATCH /api/todos/{id}/position`)
  - TODO 복제 API (`POST /api/todos/{id}/duplicate`)
  - TODO 템플릿 기능
  - TODO 태그 시스템 (다대다 관계)

- [ ] **고급 검색 기능**
  - 저장된 검색 조건
  - 복합 필터 조합 최적화
  - Full-text 검색 (MariaDB Full-text Index)

- [ ] **성능 최적화**
  - 쿼리 최적화 (N+1 문제 해결, Fetch Join)
  - 캐싱 전략 (Redis)
  - 인덱스 최적화
  - 배치 처리

- [ ] **보안 강화**
  - 비밀번호 정책 강화 (최소 길이, 복잡도)
  - Rate Limiting (Bucket4j 또는 Spring Security Rate Limiter)
  - CSRF 보호 (필요 시)
  - 입력 데이터 검증 강화

- [ ] **테스트 확대**
  - 통합 테스트 (REST API 테스트)
  - E2E 테스트
  - 테스트 커버리지 목표: 70% 이상
  - 성능 테스트

- [ ] **모니터링 및 로깅**
  - 구조화된 로깅 (JSON 형식)
  - 에러 추적 (Sentry 등)
  - 성능 모니터링 (APM)

### 🏗️ Phase 4 진행 중 - 아키텍처 및 코드 품질 개선

**기능 개요:**
코드 유지보수성, 확장성, 성능을 향상시키기 위한 아키텍처 리팩토링 및 베스트 프랙티스 적용

#### 우선순위: 높음 (필수)

**1. 커스텀 예외 처리 체계 구축 ✅ (완료)**

**구현 완료 내용:**

```java
// global/exception/BusinessException.java
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
    
    public int getStatus() {
        return errorCode.getStatus();
    }
}

// global/exception/ErrorCode.java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common (공통)
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(400, "잘못된 입력값입니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "권한이 없습니다."),
    
    // User (사용자)
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    DUPLICATE_USERNAME(409, "이미 존재하는 사용자명입니다."),
    INVALID_CREDENTIALS(401, "아이디 또는 비밀번호가 올바르지 않습니다."),
    
    // Todo (할 일)
    TODO_NOT_FOUND(404, "TODO를 찾을 수 없습니다."),
    TODO_ACCESS_DENIED(403, "TODO에 접근할 권한이 없습니다."),
    
    // Project (프로젝트)
    PROJECT_NOT_FOUND(404, "프로젝트를 찾을 수 없습니다."),
    PROJECT_ACCESS_DENIED(403, "프로젝트에 접근할 권한이 없습니다."),
    PROJECT_NAME_DUPLICATE(409, "이미 존재하는 프로젝트명입니다."),
    DEFAULT_PROJECT_DELETE_NOT_ALLOWED(400, "기본 프로젝트는 삭제할 수 없습니다."),
    DEFAULT_PROJECT_NOT_FOUND(404, "기본 프로젝트를 찾을 수 없습니다.");
    
    private final int status;
    private final String message;
}

// global/common/dto/ErrorResponse.java
@Getter
@Builder
public class ErrorResponse {
    private boolean success;
    private int status;
    private String message;
    private String code;
    private LocalDateTime timestamp;
    
    public static ErrorResponse of(ErrorCode errorCode) { ... }
    public static ErrorResponse of(ErrorCode errorCode, String customMessage) { ... }
    public static ErrorResponse of(int status, String message) { ... }
}
```

**GlobalExceptionHandler 확장:**
- ✅ `BusinessException` 핸들러 추가
- ✅ `AccessDeniedException` 핸들러 추가 (Spring Security)
- ✅ `IllegalArgumentException` 핸들러 추가
- ✅ 전역 `Exception` 핸들러 추가
- ✅ 로깅 기능 추가 (Slf4j)
- ✅ 커스텀 예외 핸들러 추가 예시 주석 작성

**Service 계층 마이그레이션 완료:**
- ✅ `TodoService`: 모든 `RuntimeException` → `BusinessException` 변경
- ✅ `ProjectService`: 모든 `IllegalArgumentException` → `BusinessException` 변경
- ✅ `AuthService`: 인증 관련 예외 → `BusinessException` 변경

**장점:**
- ✅ 타입 안전성: ErrorCode enum으로 컴파일 타임 검증
- ✅ 일관된 에러 응답: 모든 API가 동일한 에러 형식 반환
- ✅ 명확한 에러 코드: 클라이언트가 에러를 쉽게 구분 가능
- ✅ 유지보수성 향상: 에러 메시지 중앙 관리
- ✅ 로깅 개선: 예외 발생 시 자동 로깅
- ✅ 확장성: 새로운 에러 코드 추가가 간단함

**체크리스트:**
- [x] `BusinessException` 클래스 생성
- [x] `ErrorCode` enum 정의 (모든 도메인 에러)
- [x] `ErrorResponse` DTO 생성
- [x] `GlobalExceptionHandler`에 예외 핸들러 추가
  - [x] `BusinessException` 핸들러
  - [x] `Exception` 전역 핸들러
  - [x] `AccessDeniedException` 핸들러
  - [x] `IllegalArgumentException` 핸들러
- [x] 모든 Service 클래스의 예외 코드 마이그레이션
- [x] 커스텀 예외 핸들러 추가 예시 주석 작성
- [x] 테스트 코드 업데이트 (완료)
  - [x] TodoServiceTest
  - [x] ProjectServiceTest
  - [x] AuthServiceTest

**완료 시간:** 약 3시간

**테스트 결과:**
- ✅ 모든 단위 테스트 통과
- ✅ 3개 Service 테스트 모두 BusinessException 사용
- ✅ 예외 처리 일관성 100% 달성

---

**2. Specification 패턴으로 동적 쿼리 개선 (완료) ✅**

**구현 완료 내용:**
- `TodoSpecification`: 동적 쿼리를 위한 검색 조건 정의
- `TodoRepository`: `JpaSpecificationExecutor` 확장
- `TodoService`: 복잡한 if-else 쿼리 로직을 Specification 조합으로 리팩토링

```java
// domain/todo/repository/specification/TodoSpecification.java
public class TodoSpecification {
    public static Specification<Todo> hasUserId(Long userId) { ... }
    public static Specification<Todo> hasKeyword(String keyword) { ... }
    public static Specification<Todo> hasStatus(Todo.TodoStatus status) { ... }
    // ... 기타 필터 조건들
}

// TodoService 개선 결과
public Page<TodoResponse> getTodos(Long userId, TodoSearchRequest request) {
    Specification<Todo> spec = Specification
            .where(TodoSpecification.hasUserId(userId))
            .and(TodoSpecification.hasKeyword(request.getKeyword()))
            .and(TodoSpecification.hasStatus(request.getStatus()))
            .and(TodoSpecification.hasPriority(request.getPriority()))
            .and(TodoSpecification.hasProjectId(request.getProjectId()))
            .and(TodoSpecification.dueDateBetween(
                    request.getDueDateStart(), 
                    request.getDueDateEnd()
            ));
    
    Pageable pageable = createPageable(request);
    return todoRepository.findAll(spec, pageable).map(TodoResponse::from);
}
```

**체크리스트:**
- [x] `TodoSpecification` 클래스 생성
- [x] 모든 필터 조건을 Specification으로 변환
- [x] `TodoRepository`에 `JpaSpecificationExecutor` 추가
- [x] `TodoService.getTodos` 리팩토링
- [x] 기존 쿼리 메서드 제거
- [x] 단위 테스트 업데이트
- [ ] 통합 테스트 추가 (선택사항)

**완료 시간:** 약 5-6시간

**테스트 결과:**
- ✅ 모든 단위 테스트 통과 (TodoServiceTest)
- ✅ 복잡한 if-else 로직이 Specification 조합으로 간소화됨
- ✅ 코드 가독성 및 유지보수성 대폭 향상

---

**3. N+1 쿼리 문제 해결 ✅ (완료)**

**구현 완료 내용:**

```java
// 1. TodoCountByProject DTO 인터페이스 생성
public interface TodoCountByProject {
    Long getProjectId();
    Long getCount();
}

// 2. TodoRepository에 그룹화 쿼리 추가
@Query("""
    SELECT t.projectId as projectId, COUNT(t) as count
    FROM Todo t 
    WHERE t.user.id = :userId 
    GROUP BY t.projectId
    """)
List<TodoCountByProject> countByUserGroupByProjectId(@Param("userId") Long userId);

// 3. ProjectService 리팩토링
public List<ProjectResponse> getProjectsByUser(User user) {
    // 1. 사용자의 모든 프로젝트 조회 (1 query)
    List<Project> projects = projectRepository
        .findByUserOrderByPositionAscCreatedAtAsc(user);
    
    // 2. 사용자의 모든 TODO를 프로젝트별로 그룹화하여 개수 조회 (1 query)
    Map<Long, Long> todoCountMap = todoRepository
            .countByUserGroupByProjectId(user.getId())
            .stream()
            .collect(Collectors.toMap(
                    result -> result.getProjectId(),
                    result -> result.getCount()
            ));
    
    // 3. 프로젝트와 TODO 개수 매핑 (메모리 작업)
    return projects.stream()
            .map(project -> {
                Long todoCount = todoCountMap.getOrDefault(project.getId(), 0L);
                return ProjectResponse.fromWithTodoCount(project, todoCount);
            })
            .collect(Collectors.toList());
}
```

**성능 개선 효과:**
- ✅ 프로젝트 10개: **11 queries → 2 queries** (82% ↓)
- ✅ 프로젝트 100개: **101 queries → 2 queries** (98% ↓)
- ✅ 프로젝트 1000개: **1001 queries → 2 queries** (99.8% ↓)

**핵심 기술:**
- Spring Data JPA Query Projection
- GROUP BY를 활용한 집계 쿼리
- Map을 사용한 인메모리 조인

**체크리스트:**
- [x] `TodoCountByProject` DTO 인터페이스 생성
- [x] `countByUserGroupByProjectId` 쿼리 메서드 추가
- [x] `ProjectService.getProjectsByUser` 리팩토링
- [x] 쿼리 로그 설정 추가 (`use_sql_comments: true`)
- [x] 빌드 테스트 통과
- [x] 상세 문서 작성 (`N+1_QUERY_OPTIMIZATION.md`)
- [ ] `ProjectService.getProject` 최적화 (향후 작업, 단일 조회는 문제 없음)
- [ ] 실제 API 성능 테스트 (수동)

**완료 시간:** 약 2시간

**참고 문서:** `N+1_QUERY_OPTIMIZATION.md`

#### 우선순위: 중간

**4. Strategy 패턴으로 검색 로직 분리**
- 📋 [GitHub Issue #13](https://github.com/HwangGwiMan/Todo-App-Backend/issues/13)
- 예상 시간: 4-5시간
- 상세 내용은 `.github/issues/phase4-strategy-pattern.md` 참조

---

**5. MapStruct로 DTO 매핑 자동화 ✅ (완료)**

**구현 완료 내용:**

MapStruct를 사용하여 DTO ↔ Entity 변환 로직을 자동화하고, 공통 Mapper 인터페이스를 통해 일관성을 확보했습니다.

**1. 의존성 추가:**

```gradle
// build.gradle
dependencies {
    // Lombok이 먼저 실행되어야 함
    annotationProcessor 'org.projectlombok:lombok'
    
    // Lombok-MapStruct Binding (필수)
    implementation 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
    annotationProcessor 'org.projectlombok:lombok-mapstruct-binding:0.2.0'
    
    // MapStruct
    implementation 'org.mapstruct:mapstruct:1.5.5.Final'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
}
```

**2. 공통 GenericMapper 생성:**

```java
// global/common/mapper/GenericMapper.java
public interface GenericMapper<D, R, E> {
    E toEntity(D request);
    R toDto(E entity);
    List<R> toDtoList(List<E> entityList);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(D request, @MappingTarget E entity);
}
```

**3. TodoMapper 구현:**

```java
// domain/todo/mapper/TodoMapper.java
@Mapper(componentModel = "spring")
public interface TodoMapper extends GenericMapper<TodoRequest, TodoResponse, Todo> {
    
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Todo toEntity(TodoRequest request);
    
    @Override
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    TodoResponse toDto(Todo todo);
    
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateFromDto(TodoRequest request, @MappingTarget Todo todo);
}
```

**4. ProjectMapper 구현:**

```java
// domain/project/mapper/ProjectMapper.java
@Mapper(componentModel = "spring")
public interface ProjectMapper extends GenericMapper<ProjectRequest, ProjectResponse, Project> {
    
    @Override
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Project toEntity(ProjectRequest request);
    
    @Override
    @Mapping(target = "todoCount", ignore = true)
    ProjectResponse toDto(Project project);
    
    // todoCount를 포함한 매핑 추가
    @Mapping(target = "todoCount", source = "todoCount")
    ProjectResponse toDtoWithCount(Project project, Long todoCount);
    
    @Override
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateFromDto(ProjectRequest request, @MappingTarget Project project);
}
```

**5. BaseEntity @SuperBuilder 적용:**

상속 구조에서 빌더 패턴이 정상 동작하도록 수정:

```java
// global/entity/BaseEntity.java
@MappedSuperclass
@Getter
@Setter
@SuperBuilder  // @Builder → @SuperBuilder 변경
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;
}

// 모든 엔티티도 @SuperBuilder 적용
// Todo, Project, User 엔티티
@Entity
@SuperBuilder  // @Builder → @SuperBuilder 변경
public class Todo extends BaseEntity { ... }
```

**6. Service 계층 리팩토링:**

```java
// TodoService.java
@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoMapper todoMapper;
    
    @Transactional
    public TodoResponse createTodo(Long userId, TodoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Todo todo = todoMapper.toEntity(request);  // 자동 매핑
        todo.setUser(user);
        
        Todo savedTodo = todoRepository.save(todo);
        return todoMapper.toDto(savedTodo);  // 자동 매핑
    }
    
    @Transactional
    public TodoResponse updateTodo(Long userId, Long todoId, TodoRequest request) {
        Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TODO_NOT_FOUND));
        
        todoMapper.updateFromDto(request, todo);  // 자동 업데이트
        
        Todo updatedTodo = todoRepository.save(todo);
        return todoMapper.toDto(updatedTodo);
    }
    
    public Page<TodoResponse> getTodos(Long userId, TodoSearchRequest request) {
        Specification<Todo> spec = buildSpecification(userId, request);
        Pageable pageable = createPageable(request);
        
        return todoRepository.findAll(spec, pageable)
                .map(todoMapper::toDto);  // 자동 매핑
    }
}
```

**7. DTO 정리:**

수동 매핑 메서드 제거:

```java
// TodoResponse.java, ProjectResponse.java
// from(), fromWithTodoCount() 등 static 메서드 제거
// 모든 매핑은 Mapper를 통해서만 수행
```

**8. 테스트 코드 업데이트:**

```java
// ProjectServiceTest.java, TodoServiceTest.java
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    @Mock
    private ProjectMapper projectMapper;  // Mock으로 추가
    
    @InjectMocks
    private ProjectService projectService;
    
    @BeforeEach
    void setUp() {
        // lenient() 모드로 stub 설정
        lenient().when(projectMapper.toDto(any(Project.class))).thenAnswer(...);
        lenient().when(projectMapper.toDtoWithCount(any(), anyLong())).thenAnswer(...);
        lenient().when(projectMapper.toEntity(any())).thenAnswer(...);
        lenient().doAnswer(...).when(projectMapper).updateFromDto(any(), any());
    }
}
```

**장점:**
- ✅ 보일러플레이트 코드 제거
- ✅ 타입 안전한 매핑
- ✅ 컴파일 타임 검증
- ✅ 일관된 매핑 로직
- ✅ 중앙 집중화된 매핑 관리
- ✅ GenericMapper로 공통 인터페이스 제공

**체크리스트:**
- [x] MapStruct 의존성 추가 (Lombok 바인딩 포함)
- [x] `GenericMapper` 공통 인터페이스 생성
- [x] `TodoMapper` 인터페이스 생성
- [x] `ProjectMapper` 인터페이스 생성
- [x] `BaseEntity`에 `@SuperBuilder` 적용
- [x] 모든 엔티티에 `@SuperBuilder` 적용
- [x] Service 계층에서 수동 매핑 제거
- [x] DTO의 static 매핑 메서드 제거
- [x] 빌드 확인 (매퍼 구현체 자동 생성)
- [x] 테스트 코드 업데이트 (lenient 모드 적용)

**완료 시간:** 약 5시간

**테스트 결과:**
- ✅ 모든 단위 테스트 통과
- ✅ MapStruct 구현체 정상 생성 (`TodoMapperImpl`, `ProjectMapperImpl`)
- ✅ Service 계층 매핑 로직 자동화 완료

---

**6. Spring Events로 관심사 분리 ✅ (완료)**
- 📋 [GitHub Issue #14](https://github.com/HwangGwiMan/Todo-App-Backend/issues/14)
- 예상 시간: 3-4시간
- 상세 내용은 `.github/issues/phase4-spring-events.md` 참조

---

**7. 캐싱 전략 구현 ✅ (완료)**

**구현 완료 내용:**

Spring Cache를 활용하여 자주 조회되는 데이터에 대한 캐싱 전략을 구현했습니다.

**1. CacheConfig 설정:**

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
            new ConcurrentMapCache("projectList"),
            new ConcurrentMapCache("stats")
        ));
        return cacheManager;
    }
}
```

**2. TodoService 캐싱 적용:**

- `@Cacheable`: getTodo, getUserStats, getDashboardStats
- `@CacheEvict`: createTodo, updateTodo, updateTodoStatus, deleteTodo
- `@Caching` 어노테이션으로 여러 캐시 동시 무효화

**3. ProjectService 캐싱 적용:**

- `@Cacheable`: getProject, getProjectsByUser, getDefaultProject
- `@CacheEvict`: createProject, updateProject, deleteProject
- `@Caching` 어노테이션으로 여러 캐시 동시 무효화

**캐시 키 전략:**
- TODO: `todos:userId:{userId}:todoId:{todoId}`
- 통계: `stats:user:{userId}`, `stats:dashboard:{userId}`
- 프로젝트: `projects:userId:{userId}:projectId:{projectId}`, `projects:default:userId:{userId}`
- 프로젝트 목록: `projectList:userId:{userId}`

**장점:**
- ✅ 조회 성능 향상 (캐시 히트 시)
- ✅ 데이터베이스 부하 감소
- ✅ 응답 시간 단축
- ✅ 캐시 무효화 전략으로 데이터 일관성 보장

**체크리스트:**
- [x] @EnableCaching 설정
- [x] CacheManager 빈 등록
- [x] 주요 조회 메서드에 @Cacheable 적용
- [x] 수정/삭제 메서드에 @CacheEvict 적용
- [x] 캐시 키 전략 설계
- [x] 캐시 모니터링 로그 추가 (DEBUG 레벨)
- [ ] 성능 테스트 (수동 테스트 필요)

**완료 시간:** 약 3시간

**상세 내용:** `.github/issues/phase4-caching.md` 참조

**향후 개선:**
- [ ] Redis 연동 (프로덕션 환경)
- [ ] 캐시 TTL 설정 (Caffeine 또는 Redis 사용 시)
- [ ] 분산 캐시 전략

#### 우선순위: 낮음 (선택)

**8. 감사 로그 시스템**
- 📋 [GitHub Issue #16](https://github.com/HwangGwiMan/Todo-App-Backend/issues/16)
- 예상 시간: 5-6시간
- 상세 내용은 `.github/issues/phase4-audit-log.md` 참조

---

**9. Rate Limiting 구현**
- 📋 [GitHub Issue #17](https://github.com/HwangGwiMan/Todo-App-Backend/issues/17)
- 예상 시간: 2-3시간
- 상세 내용은 `.github/issues/phase4-rate-limiting.md` 참조

---

**10. Soft Delete 구현**
- 📋 [GitHub Issue #18](https://github.com/HwangGwiMan/Todo-App-Backend/issues/18)
- 예상 시간: 2-3시간
- 상세 내용은 `.github/issues/phase4-soft-delete.md` 참조

#### 추가 개선사항

**11. JOOQ 타입 안전성 개선**
- 📋 [GitHub Issue #19](https://github.com/HwangGwiMan/Todo-App-Backend/issues/19)
- 예상 시간: 4-5시간
- 상세 내용은 `.github/issues/phase4-jooq-type-safety.md` 참조

---

**12. 입력 검증 강화**
- 📋 [GitHub Issue #20](https://github.com/HwangGwiMan/Todo-App-Backend/issues/20)
- 예상 시간: 2-3시간
- 상세 내용은 `.github/issues/phase4-input-validation.md` 참조

#### 총 예상 개발 시간 (Phase 4)

**우선순위 높음 (필수):** 11-14시간 ✅ 완료
- ~~예외 처리 체계~~
- ~~Specification 패턴~~
- ~~N+1 문제 해결~~
- ~~MapStruct~~

**우선순위 중간 (권장):** 7-11시간
- Strategy 패턴으로 검색 로직 분리 (4-5시간 예상)
- ~~캐싱 전략 구현~~ ✅ 완료 (3-4시간 소요)

**우선순위 낮음 (선택):** 9-12시간
- 감사 로그: [Issue #16](https://github.com/HwangGwiMan/Todo-App-Backend/issues/16)
- Rate Limiting: [Issue #17](https://github.com/HwangGwiMan/Todo-App-Backend/issues/17)
- Soft Delete: [Issue #18](https://github.com/HwangGwiMan/Todo-App-Backend/issues/18)

**추가 개선:** 6-8시간
- JOOQ 타입 안전성: [Issue #19](https://github.com/HwangGwiMan/Todo-App-Backend/issues/19)
- 입력 검증 강화: [Issue #20](https://github.com/HwangGwiMan/Todo-App-Backend/issues/20)

---

### 📅 Phase 6 - TODO 일정 관리 및 알림 기능

**📋 [GitHub Issue #21](https://github.com/HwangGwiMan/Todo-App-Backend/issues/21)**

**기능 개요:** TODO에 상세한 일정 관리 필드를 추가하고, 카카오톡/SMS/이메일을 통한 알림 기능 구현

**예상 소요 시간:** 27-35시간

**주요 구현 내용:**
- 일정 관리 필드 확장 (startDate, endDate, isAllDay, recurrenceRule, location 등)
- 알림 시스템 (이메일, SMS, 카카오톡 알림톡)
- 알림 스케줄러 (Spring Scheduler)
- 반복 일정 처리
- 사용자 알림 설정

**상세 내용:** `.github/issues/phase6-notification-system.md` 참조

---

### 📤 Phase 5 - 파일 출력(Export) 기능

**📋 [GitHub Issue #22](https://github.com/HwangGwiMan/Todo-App-Backend/issues/22)**

**기능 개요:** TODO 및 프로젝트 데이터를 다양한 파일 형식으로 내보내기 (JSON, Excel, PDF)

**예상 소요 시간:** 10-14시간

**지원 파일 형식:**
- JSON 출력 (1-2시간) - 높은 우선순위
- Excel 출력 (3-4시간) - 높은 우선순위, Apache POI 사용
- PDF 출력 (4-5시간) - 중간 우선순위, iText7 사용

**상세 내용:** `.github/issues/phase5-file-export.md` 참조

---

### 🏗️ 이전 Phase 완료 이력

- ✅ **Phase 1 완료**: 인증 시스템, TODO CRUD, 검색/필터링, 통계 API
- ✅ **Phase 2 완료**: 프로젝트 관리, 프로젝트-TODO 연동, position 관리
- ✅ **Phase 3 완료**: 날짜 범위 검색
- 🚧 **Phase 4 진행 중**: 아키텍처 개선 및 코드 품질 향상 (우선순위 높음 항목 완료, 캐싱 전략 완료)

## 🔧 설정

**구현 계획:**

```java
// global/audit/AuditLog.java
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

// AOP로 자동 감사
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

**체크리스트:**
- [ ] `AuditLog` 엔티티 생성
- [ ] `@Auditable` 어노테이션 정의
- [ ] `AuditAspect` 구현
- [ ] Service 메서드에 `@Auditable` 적용
- [ ] 감사 로그 조회 API
- [ ] 테스트

**예상 시간:** 5-6시간

---

**9. Rate Limiting 구현 (2-3시간)**

**구현 계획:**

```java
// Google Guava RateLimiter 사용
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

// 사용 예시
@RateLimit(permitsPerSecond = 10.0)
@PostMapping
public ResponseEntity<?> createTodo(@RequestBody TodoRequest request) {
    // 초당 10개 요청 제한
}
```

**체크리스트:**
- [ ] Guava 의존성 추가
- [ ] `@RateLimit` 어노테이션 정의
- [ ] `RateLimitAspect` 구현
- [ ] Controller에 적용
- [ ] ErrorCode 추가 (TOO_MANY_REQUESTS)
- [ ] 테스트

**예상 시간:** 2-3시간

---

**10. Soft Delete 구현 (2-3시간)**

**구현 계획:**

```java
// Todo 엔티티에 추가
@Entity
@SQLDelete(sql = "UPDATE todos SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Todo extends BaseEntity {
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}

// 복구 API 추가
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

**체크리스트:**
- [ ] 엔티티에 `deletedAt` 필드 추가
- [ ] `@SQLDelete`, `@Where` 어노테이션 적용
- [ ] 복구 API 구현
- [ ] 휴지통 조회 API
- [ ] 영구 삭제 API (관리자용)
- [ ] 테스트

**예상 시간:** 2-3시간

#### 추가 개선사항

**11. JOOQ 타입 안전성 개선 (4-5시간)**

**현재 문제:**
- `TodoRepositoryImpl`에서 문자열 기반 필드명 사용
- 리팩토링 시 런타임 오류 위험

**구현 계획:**

```gradle
plugins {
    id 'nu.studer.jooq' version '8.2'
}

jooq {
    configurations {
        main {
            generateSchemaSourceOnCompilation = true
            generationTool {
                jdbc {
                    driver = 'org.mariadb.jdbc.Driver'
                    url = 'jdbc:mariadb://localhost:3306/todoapp'
                }
                generator {
                    database {
                        name = 'org.jooq.meta.mariadb.MariaDBDatabase'
                    }
                    target {
                        packageName = 'com.TodoApp.backend.jooq'
                        directory = 'build/generated-src/jooq/main'
                    }
                }
            }
        }
    }
}
```

**체크리스트:**
- [ ] JOOQ Gradle 플러그인 설정
- [ ] 코드 생성 실행
- [ ] `TodoRepositoryImpl` 리팩토링
- [ ] 문자열 필드명을 타입 안전 코드로 변경
- [ ] 빌드 스크립트 업데이트

**예상 시간:** 4-5시간

---

**12. 입력 검증 강화 (2-3시간)**

```java
// TodoRequest 개선
public class TodoRequest {
    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 255, message = "제목은 1-255자여야 합니다")
    private String title;
    
    @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
    private String description;
    
    @Min(value = 0, message = "position은 0 이상이어야 합니다")
    private Integer position;
}

// ProjectRequest 개선
public class ProjectRequest {
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "유효하지 않은 색상 코드")
    private String color;
}
```

**체크리스트:**
- [ ] 모든 Request DTO에 validation 어노테이션 추가
- [ ] Custom Validator 작성 (필요 시)
- [ ] 에러 메시지 한글화
- [ ] Validation 실패 테스트 작성

**예상 시간:** 2-3시간

#### 총 예상 개발 시간

**우선순위 높음 (필수):** 11-14시간
- 예외 처리 체계: 3-4시간
- Specification 패턴: 5-6시간
- N+1 문제 해결: 3-4시간

**우선순위 중간 (권장):** 7-11시간
- Strategy 패턴: 4-5시간 (Specification 구현 시 선택)
- 캐싱: 3-4시간

**우선순위 낮음 (선택):** 11-15시간
- 감사 로그: 5-6시간
- Rate Limiting: 2-3시간
- Soft Delete: 2-3시간

**추가 개선:** 6-8시간
- JOOQ 타입 안전성: 4-5시간
- 입력 검증 강화: 2-3시간

**총합:** 39-51시간

---

### 📅 Phase 6 예정 - TODO 일정 관리 및 알림 기능

**기능 개요:**
TODO에 상세한 일정 관리 필드를 추가하고, 카카오톡/SMS/이메일을 통한 알림 기능을 구현합니다.

#### 1. TODO 엔티티 확장 - 일정 관리 필드

**추가될 필드:**

```java
// Todo.java 엔티티에 추가할 필드들

@Entity
public class Todo {
    // ... 기존 필드들 ...
    
    // === 일정 관련 필드 ===
    
    /**
     * 일정 시작 일시 (선택)
     * 시작 시간이 있는 TODO인 경우 사용
     */
    @Column(name = "start_date")
    private Timestamp startDate;
    
    /**
     * 일정 종료 일시 (선택)
     * 종료 시간이 있는 TODO인 경우 사용 (기존 dueDate와 별개)
     */
    @Column(name = "end_date")
    private Timestamp endDate;
    
    /**
     * 종일 일정 여부
     * true: 종일 일정 (시간 무시)
     * false: 시간 포함 일정
     */
    @Column(name = "is_all_day", nullable = false)
    private Boolean isAllDay = false;
    
    /**
     * 반복 일정 설정 (JSON 형식)
     * 예: {"type": "DAILY", "interval": 1, "endDate": "2025-12-31"}
     * type: NONE, DAILY, WEEKLY, MONTHLY, YEARLY
     * interval: 반복 간격 (예: 2일마다, 3주마다)
     * daysOfWeek: 요일 선택 (주간 반복 시) [1-7, 월-일]
     * dayOfMonth: 날짜 선택 (월간 반복 시) [1-31]
     * endDate: 반복 종료일 (선택)
     * count: 반복 횟수 (선택, endDate와 배타적)
     */
    @Column(name = "recurrence_rule", columnDefinition = "TEXT")
    private String recurrenceRule;
    
    /**
     * 원본 TODO ID (반복 일정의 경우)
     * 반복 일정에서 생성된 개별 인스턴스는 이 필드로 원본을 참조
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_todo_id")
    private Todo parentTodo;
    
    /**
     * 일정 위치 정보 (선택)
     * 예: "서울시 강남구 테헤란로 123"
     */
    @Column(name = "location", length = 500)
    private String location;
    
    /**
     * 예상 소요 시간 (분 단위)
     * 예: 30분, 120분 (2시간)
     */
    @Column(name = "estimated_duration")
    private Integer estimatedDuration;
    
    // === 알림 관련 필드 ===
    
    /**
     * 알림 설정 (JSON 배열 형식)
     * 예: [{"type": "KAKAO", "timing": -30}, {"type": "SMS", "timing": -60}]
     * type: EMAIL, SMS, KAKAO, PUSH
     * timing: 알림 시간 (분 단위, 음수는 사전 알림)
     *   -30: 30분 전
     *   -60: 1시간 전
     *   -1440: 1일 전
     *   0: 정시
     */
    @Column(name = "notification_settings", columnDefinition = "TEXT")
    private String notificationSettings;
    
    /**
     * 알림 활성화 여부
     */
    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled = false;
}
```

**DTO 확장:**

```java
// TodoRequest.java
public class TodoRequest {
    // ... 기존 필드들 ...
    
    @Nullable
    @Schema(nullable = true, description = "일정 시작 일시")
    private Timestamp startDate;
    
    @Nullable
    @Schema(nullable = true, description = "일정 종료 일시")
    private Timestamp endDate;
    
    @Nullable
    @Schema(nullable = true, description = "종일 일정 여부", defaultValue = "false")
    private Boolean isAllDay;
    
    @Nullable
    @Schema(nullable = true, description = "반복 설정 (JSON)")
    private String recurrenceRule;
    
    @Nullable
    @Schema(nullable = true, description = "일정 위치")
    private String location;
    
    @Nullable
    @Schema(nullable = true, description = "예상 소요 시간 (분)")
    private Integer estimatedDuration;
    
    @Nullable
    @Schema(nullable = true, description = "알림 설정 (JSON 배열)")
    private String notificationSettings;
    
    @Nullable
    @Schema(nullable = true, description = "알림 활성화 여부", defaultValue = "false")
    private Boolean notificationEnabled;
}

// TodoResponse.java
public class TodoResponse {
    // ... 기존 필드들 ...
    
    private Timestamp startDate;
    private Timestamp endDate;
    private Boolean isAllDay;
    private String recurrenceRule;
    private String location;
    private Integer estimatedDuration;
    private String notificationSettings;
    private Boolean notificationEnabled;
    private Long parentTodoId;  // 반복 일정의 원본 ID
}
```

#### 2. 알림 시스템 구조

**새로운 도메인 패키지: notification**

```
domain/notification/
├── controller/
│   └── NotificationController.java     # 알림 테스트 및 설정 API
├── dto/
│   ├── NotificationRequest.java
│   ├── NotificationSettingDto.java
│   └── NotificationResponse.java
├── entity/
│   ├── NotificationLog.java            # 알림 발송 이력
│   └── NotificationSetting.java        # 사용자별 알림 설정
├── repository/
│   ├── NotificationLogRepository.java
│   └── NotificationSettingRepository.java
├── service/
│   ├── NotificationService.java        # 알림 관리 총괄
│   ├── EmailNotificationService.java   # 이메일 알림
│   ├── SmsNotificationService.java     # SMS 알림
│   ├── KakaoNotificationService.java   # 카카오톡 알림톡
│   └── PushNotificationService.java    # 브라우저 푸시 (선택)
└── scheduler/
    └── NotificationScheduler.java      # 알림 스케줄링 (Spring Scheduler)
```

**NotificationLog 엔티티:**

```java
@Entity
@Table(name = "notification_logs")
public class NotificationLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;  // EMAIL, SMS, KAKAO, PUSH
    
    @Column(nullable = false, length = 50)
    private String status;  // PENDING, SENT, FAILED, CANCELLED
    
    @Column(name = "scheduled_time", nullable = false)
    private Timestamp scheduledTime;  // 발송 예정 시간
    
    @Column(name = "sent_time")
    private Timestamp sentTime;  // 실제 발송 시간
    
    @Column(name = "recipient", length = 255)
    private String recipient;  // 수신자 정보 (이메일, 전화번호 등)
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;  // 발송된 메시지 내용
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;  // 실패 시 에러 메시지
}
```

#### 3. 알림 서비스 구현 계획

**3-1. 카카오톡 알림톡 (우선순위: 높음)**

**필요 사항:**
- 카카오 비즈니스 계정 등록
- 알림톡 템플릿 승인 (카카오 검수 필요)
- Kakao Notification API 연동

**라이브러리:**
```gradle
// build.gradle
implementation 'org.springframework.boot:spring-boot-starter-webflux'  // WebClient 사용
```

**구현 예시:**

```java
@Service
@RequiredArgsConstructor
public class KakaoNotificationService {
    
    private final WebClient kakaoWebClient;
    
    @Value("${kakao.api.key}")
    private String kakaoApiKey;
    
    @Value("${kakao.sender.key}")
    private String senderKey;
    
    /**
     * 카카오톡 알림톡 발송
     * 
     * @param phoneNumber 수신자 전화번호 (010-1234-5678)
     * @param templateCode 템플릿 코드 (카카오 승인된 템플릿)
     * @param params 템플릿 변수 (Map)
     */
    public void sendKakaoNotification(
        String phoneNumber, 
        String templateCode,
        Map<String, String> params
    ) {
        try {
            String response = kakaoWebClient
                .post()
                .uri("/v2/api/send/ata/send")
                .header("Authorization", "Bearer " + kakaoApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                    "senderKey", senderKey,
                    "phoneNumber", phoneNumber.replace("-", ""),
                    "templateCode", templateCode,
                    "templateParameter", params
                ))
                .retrieve()
                .bodyToMono(String.class)
                .block();
            
            log.info("카카오톡 알림 발송 성공: {}", response);
        } catch (Exception e) {
            log.error("카카오톡 알림 발송 실패: {}", e.getMessage());
            throw new RuntimeException("카카오톡 알림 발송 실패", e);
        }
    }
    
    /**
     * TODO 알림 발송
     */
    public void sendTodoNotification(User user, Todo todo, int minutesBefore) {
        String phoneNumber = user.getPhoneNumber();
        
        Map<String, String> params = new HashMap<>();
        params.put("userName", user.getUsername());
        params.put("todoTitle", todo.getTitle());
        params.put("dueDate", formatDate(todo.getDueDate()));
        params.put("timeUntil", formatTimeUntil(minutesBefore));
        
        // 템플릿 예시:
        // [TodoApp 알림]
        // #{userName}님, "#{todoTitle}" 할 일이 #{timeUntil} 남았습니다.
        // 마감: #{dueDate}
        
        sendKakaoNotification(phoneNumber, "TODO_REMINDER", params);
    }
}
```

**3-2. SMS 문자 알림 (우선순위: 중간)**

**SMS API 제공 업체 선택:**
- NHN Cloud SMS (구 Toast Cloud)
- Twilio
- 알리고 (Aligo)
- 솔라피 (Solapi)

**구현 예시 (NHN Cloud SMS):**

```java
@Service
@RequiredArgsConstructor
public class SmsNotificationService {
    
    @Value("${nhn.sms.app-key}")
    private String appKey;
    
    @Value("${nhn.sms.secret-key}")
    private String secretKey;
    
    @Value("${nhn.sms.sender-number}")
    private String senderNumber;
    
    private final RestTemplate restTemplate;
    
    public void sendSms(String recipient, String message) {
        String url = "https://api-sms.cloud.toast.com/sms/v3.0/appKeys/" 
                     + appKey + "/sender/sms";
        
        Map<String, Object> body = Map.of(
            "body", message,
            "sendNo", senderNumber,
            "recipientList", List.of(Map.of("recipientNo", recipient))
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Secret-Key", secretKey);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url, request, String.class
            );
            log.info("SMS 발송 성공: {}", response.getBody());
        } catch (Exception e) {
            log.error("SMS 발송 실패: {}", e.getMessage());
            throw new RuntimeException("SMS 발송 실패", e);
        }
    }
    
    public void sendTodoReminder(User user, Todo todo, int minutesBefore) {
        String message = String.format(
            "[TodoApp] %s님, \"%s\" 할 일이 %d분 후 마감됩니다.",
            user.getUsername(),
            todo.getTitle(),
            minutesBefore
        );
        
        sendSms(user.getPhoneNumber(), message);
    }
}
```

**3-3. 이메일 알림 (우선순위: 중간)**

```java
@Service
@RequiredArgsConstructor
public class EmailNotificationService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.from}")
    private String fromEmail;
    
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);  // HTML 지원
            
            mailSender.send(message);
            log.info("이메일 발송 성공: {}", to);
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", e.getMessage());
            throw new RuntimeException("이메일 발송 실패", e);
        }
    }
    
    public void sendTodoReminder(User user, Todo todo, int minutesBefore) {
        String subject = "[TodoApp] TODO 알림: " + todo.getTitle();
        String body = String.format("""
            <html>
            <body>
                <h2>TODO 알림</h2>
                <p>안녕하세요, %s님!</p>
                <p>다음 할 일이 <strong>%d분 후</strong> 마감됩니다:</p>
                <div style="padding: 15px; background: #f5f5f5; border-radius: 5px;">
                    <h3>%s</h3>
                    <p>%s</p>
                    <p>마감: %s</p>
                </div>
            </body>
            </html>
            """,
            user.getUsername(),
            minutesBefore,
            todo.getTitle(),
            todo.getDescription() != null ? todo.getDescription() : "",
            formatDate(todo.getDueDate())
        );
        
        sendEmail(user.getEmail(), subject, body);
    }
}
```

#### 4. 알림 스케줄러 구현

```java
@Component
@RequiredArgsConstructor
@EnableScheduling
public class NotificationScheduler {
    
    private final TodoRepository todoRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final EmailNotificationService emailService;
    private final SmsNotificationService smsService;
    private final KakaoNotificationService kakaoService;
    
    /**
     * 매 분마다 실행되어 알림이 필요한 TODO를 확인하고 알림 발송
     */
    @Scheduled(cron = "0 * * * * *")  // 매 분 0초에 실행
    public void checkAndSendNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkUntil = now.plusMinutes(60);  // 1시간 이내 알림 확인
        
        // 알림이 활성화되고 마감일이 다가오는 TODO 조회
        List<Todo> upcomingTodos = todoRepository.findUpcomingTodosWithNotification(
            Timestamp.valueOf(now), 
            Timestamp.valueOf(checkUntil)
        );
        
        for (Todo todo : upcomingTodos) {
            try {
                sendNotificationsForTodo(todo);
            } catch (Exception e) {
                log.error("알림 발송 실패 - TODO ID: {}, 에러: {}", 
                    todo.getId(), e.getMessage());
            }
        }
    }
    
    private void sendNotificationsForTodo(Todo todo) {
        if (!todo.getNotificationEnabled() || 
            todo.getNotificationSettings() == null) {
            return;
        }
        
        // JSON 파싱
        List<NotificationSetting> settings = parseNotificationSettings(
            todo.getNotificationSettings()
        );
        
        for (NotificationSetting setting : settings) {
            // 이미 발송된 알림인지 확인
            if (isAlreadySent(todo, setting)) {
                continue;
            }
            
            // 알림 시간 계산
            LocalDateTime notificationTime = calculateNotificationTime(
                todo.getDueDate(), 
                setting.getTiming()
            );
            
            // 현재 시간이 알림 시간을 지났는지 확인
            if (LocalDateTime.now().isAfter(notificationTime)) {
                sendNotification(todo, setting);
                logNotification(todo, setting, "SENT");
            }
        }
    }
    
    private void sendNotification(Todo todo, NotificationSetting setting) {
        User user = todo.getUser();
        int minutesUntil = Math.abs(setting.getTiming());
        
        switch (setting.getType()) {
            case EMAIL:
                emailService.sendTodoReminder(user, todo, minutesUntil);
                break;
            case SMS:
                smsService.sendTodoReminder(user, todo, minutesUntil);
                break;
            case KAKAO:
                kakaoService.sendTodoNotification(user, todo, minutesUntil);
                break;
            case PUSH:
                // 브라우저 푸시 알림 (선택)
                break;
        }
    }
}
```

#### 5. 반복 일정 처리

```java
@Service
@RequiredArgsConstructor
public class RecurrenceService {
    
    /**
     * 반복 규칙에 따라 다음 발생 날짜 계산
     */
    public List<LocalDateTime> calculateOccurrences(
        String recurrenceRule,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        RecurrenceRule rule = parseRecurrenceRule(recurrenceRule);
        List<LocalDateTime> occurrences = new ArrayList<>();
        
        LocalDateTime current = startDate;
        int count = 0;
        
        while (true) {
            // 종료 조건 확인
            if (rule.getEndDate() != null && current.isAfter(rule.getEndDate())) {
                break;
            }
            if (rule.getCount() != null && count >= rule.getCount()) {
                break;
            }
            if (endDate != null && current.isAfter(endDate)) {
                break;
            }
            
            occurrences.add(current);
            count++;
            
            // 다음 발생 날짜 계산
            current = getNextOccurrence(current, rule);
        }
        
        return occurrences;
    }
    
    private LocalDateTime getNextOccurrence(
        LocalDateTime current, 
        RecurrenceRule rule
    ) {
        return switch (rule.getType()) {
            case DAILY -> current.plusDays(rule.getInterval());
            case WEEKLY -> current.plusWeeks(rule.getInterval());
            case MONTHLY -> current.plusMonths(rule.getInterval());
            case YEARLY -> current.plusYears(rule.getInterval());
            default -> current;
        };
    }
}
```

#### 6. API 엔드포인트 추가

| Method | Endpoint | 설명 | 인증 필요 |
|--------|----------|------|----------|
| GET | `/api/notifications/settings` | 사용자 알림 설정 조회 | ✅ |
| PUT | `/api/notifications/settings` | 사용자 알림 설정 수정 | ✅ |
| POST | `/api/notifications/test` | 테스트 알림 발송 | ✅ |
| GET | `/api/notifications/logs` | 알림 발송 이력 조회 | ✅ |
| GET | `/api/todos/{id}/recurrence` | 반복 일정 미리보기 | ✅ |

#### 7. 환경 설정

```yaml
# application.yml에 추가

# 카카오톡 알림톡 설정
kakao:
  api:
    key: ${KAKAO_API_KEY}
    url: https://kapi.kakao.com
  sender:
    key: ${KAKAO_SENDER_KEY}

# SMS 설정 (NHN Cloud)
nhn:
  sms:
    app-key: ${NHN_SMS_APP_KEY}
    secret-key: ${NHN_SMS_SECRET_KEY}
    sender-number: ${SMS_SENDER_NUMBER}

# 이메일 설정
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
    from: ${EMAIL_FROM}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

# 스케줄러 설정
scheduling:
  enabled: true
  notification:
    check-interval: 60000  # 60초마다 확인
```

#### 8. 구현 우선순위 및 예상 시간

**Phase 5-1: 일정 필드 확장 (4-5시간)**
- [ ] Todo 엔티티 확장 (startDate, endDate, isAllDay 등)
- [ ] DTO 및 매핑 로직 수정
- [ ] 데이터베이스 마이그레이션 스크립트
- [ ] API 문서 업데이트 (Swagger)

**Phase 5-2: 이메일 알림 (3-4시간)**
- [ ] JavaMailSender 설정
- [ ] EmailNotificationService 구현
- [ ] 이메일 템플릿 작성 (HTML)
- [ ] 테스트 API 구현

**Phase 5-3: 알림 스케줄러 (4-5시간)**
- [ ] NotificationLog 엔티티 및 Repository
- [ ] NotificationScheduler 구현
- [ ] 알림 설정 파싱 로직
- [ ] 중복 발송 방지 로직

**Phase 5-4: SMS 알림 (3-4시간)**
- [ ] SMS API 업체 선택 및 계정 생성
- [ ] SmsNotificationService 구현
- [ ] 테스트 및 에러 처리

**Phase 5-5: 카카오톡 알림톡 (5-6시간)**
- [ ] 카카오 비즈니스 계정 등록
- [ ] 알림톡 템플릿 작성 및 승인 요청
- [ ] KakaoNotificationService 구현
- [ ] 테스트 및 에러 처리

**Phase 5-6: 반복 일정 (6-8시간)**
- [ ] RecurrenceService 구현
- [ ] 반복 규칙 파싱 및 검증
- [ ] 다음 발생 날짜 계산 로직
- [ ] 반복 일정 미리보기 API

**Phase 5-7: 사용자 알림 설정 (2-3시간)**
- [ ] NotificationSetting 엔티티
- [ ] 사용자별 알림 설정 CRUD API
- [ ] 전역 알림 on/off 기능

**총 예상 개발 시간: 27-35시간**

#### 9. 테스트 계획

- [ ] 단위 테스트: NotificationService, RecurrenceService
- [ ] 통합 테스트: 알림 발송 플로우
- [ ] 스케줄러 테스트: 시간대별 알림 발송
- [ ] E2E 테스트: TODO 생성부터 알림 수신까지

#### 10. 참고 문서

- [Kakao Notification API 문서](https://developers.kakao.com/docs/latest/ko/message/rest-api)
- [NHN Cloud SMS API 문서](https://docs.nhncloud.com/ko/Notification/SMS/ko/api-guide/)
- [Spring Scheduler 가이드](https://spring.io/guides/gs/scheduling-tasks/)
- [Spring Mail 가이드](https://docs.spring.io/spring-framework/reference/integration/email.html)

### 📤 Phase 5 예정 - 파일 출력(Export) 기능

**기능 개요:**
TODO 및 프로젝트 데이터를 다양한 파일 형식으로 내보내기할 수 있는 기능 추가

#### 지원 예정 파일 형식

**1. JSON 출력 (우선순위: 높음)**
- **구현 난이도**: ⭐ (낮음)
- **예상 소요 시간**: 1-2시간
- **필요 라이브러리**: 없음 (Spring Boot 기본 제공)
- **API 엔드포인트 추가 예정**:
  ```java
  // TodoExportController.java (신규 생성)
  
  @GetMapping("/{todoId}/export/json")
  public ResponseEntity<TodoResponse> exportTodoAsJson(
      @AuthenticationPrincipal User user,
      @PathVariable Long todoId
  ) {
      TodoResponse todo = todoService.getTodo(user.getId(), todoId);
      
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, 
                  "attachment; filename=todo_" + todoId + ".json")
          .contentType(MediaType.APPLICATION_JSON)
          .body(todo);
  }
  
  @GetMapping("/export/json")
  public ResponseEntity<List<TodoResponse>> exportTodosAsJson(
      @AuthenticationPrincipal User user,
      @ModelAttribute TodoSearchRequest searchRequest
  ) {
      Page<TodoResponse> todos = todoService.getTodos(user.getId(), searchRequest);
      
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, 
                  "attachment; filename=todos_" + LocalDate.now() + ".json")
          .contentType(MediaType.APPLICATION_JSON)
          .body(todos.getContent());
  }
  
  @PostMapping("/export/json")
  public ResponseEntity<List<TodoResponse>> exportSelectedTodosAsJson(
      @AuthenticationPrincipal User user,
      @RequestBody List<Long> todoIds
  ) {
      List<TodoResponse> todos = todoService.getTodosByIds(user.getId(), todoIds);
      
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, 
                  "attachment; filename=todos_selected_" + LocalDate.now() + ".json")
          .contentType(MediaType.APPLICATION_JSON)
          .body(todos);
  }
  ```

**2. Excel 출력 (우선순위: 높음)**
- **구현 난이도**: ⭐⭐ (중간)
- **예상 소요 시간**: 3-4시간
- **필요 라이브러리**: Apache POI
  ```gradle
  // build.gradle에 추가
  implementation 'org.apache.poi:poi:5.2.5'
  implementation 'org.apache.poi:poi-ooxml:5.2.5'
  ```
- **API 엔드포인트 추가 예정**:
  ```java
  @GetMapping("/export/excel")
  public ResponseEntity<byte[]> exportTodosAsExcel(
      @AuthenticationPrincipal User user,
      @ModelAttribute TodoSearchRequest searchRequest
  ) throws IOException {
      Page<TodoResponse> todos = todoService.getTodos(user.getId(), searchRequest);
      
      Workbook workbook = new XSSFWorkbook();
      Sheet sheet = workbook.createSheet("Todos");
      
      // 헤더 행 생성
      Row headerRow = sheet.createRow(0);
      String[] headers = {"ID", "제목", "설명", "상태", "우선순위", 
                          "마감일", "생성일", "수정일", "완료일", "프로젝트"};
      for (int i = 0; i < headers.length; i++) {
          Cell cell = headerRow.createCell(i);
          cell.setCellValue(headers[i]);
          // 스타일링 적용 (굵게, 배경색 등)
      }
      
      // 데이터 행 생성
      int rowNum = 1;
      for (TodoResponse todo : todos.getContent()) {
          Row row = sheet.createRow(rowNum++);
          row.createCell(0).setCellValue(todo.getId());
          row.createCell(1).setCellValue(todo.getTitle());
          row.createCell(2).setCellValue(todo.getDescription());
          row.createCell(3).setCellValue(todo.getStatus());
          row.createCell(4).setCellValue(todo.getPriority());
          // ... 나머지 필드들
      }
      
      // 열 너비 자동 조정
      for (int i = 0; i < headers.length; i++) {
          sheet.autoSizeColumn(i);
      }
      
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      workbook.write(outputStream);
      workbook.close();
      
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, 
                  "attachment; filename=todos_" + LocalDate.now() + ".xlsx")
          .contentType(MediaType.parseMediaType(
                  "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
          .body(outputStream.toByteArray());
  }
  ```
- **Excel 스타일링**:
  - 헤더 행: 굵게, 배경색 (회색)
  - 상태별 색상 코딩 (TODO: 파랑, IN_PROGRESS: 주황, DONE: 초록)
  - 우선순위별 색상 (HIGH: 빨강, MEDIUM: 노랑, LOW: 회색)

**3. PDF 출력 (우선순위: 중간)**
- **구현 난이도**: ⭐⭐⭐ (높음)
- **예상 소요 시간**: 4-5시간
- **필요 라이브러리**: iText7
  ```gradle
  // build.gradle에 추가
  implementation 'com.itextpdf:itext7-core:7.2.5'
  ```
- **API 엔드포인트 추가 예정**:
  ```java
  @GetMapping("/{todoId}/export/pdf")
  public ResponseEntity<byte[]> exportTodoAsPdf(
      @AuthenticationPrincipal User user,
      @PathVariable Long todoId
  ) throws IOException {
      TodoResponse todo = todoService.getTodo(user.getId(), todoId);
      
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      PdfWriter writer = new PdfWriter(outputStream);
      PdfDocument pdfDoc = new PdfDocument(writer);
      Document document = new Document(pdfDoc);
      
      // 제목
      document.add(new Paragraph(todo.getTitle())
          .setFontSize(20)
          .setBold());
      
      // 메타 정보
      Table metaTable = new Table(2);
      metaTable.addCell("상태");
      metaTable.addCell(todo.getStatus());
      metaTable.addCell("우선순위");
      metaTable.addCell(todo.getPriority());
      metaTable.addCell("마감일");
      metaTable.addCell(todo.getDueDate() != null ? todo.getDueDate().toString() : "-");
      metaTable.addCell("생성일");
      metaTable.addCell(todo.getCreatedAt().toString());
      document.add(metaTable);
      
      // 설명
      document.add(new Paragraph("\n설명:").setBold());
      document.add(new Paragraph(todo.getDescription() != null ? todo.getDescription() : "-"));
      
      document.close();
      
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, 
                  "attachment; filename=todo_" + todoId + ".pdf")
          .contentType(MediaType.APPLICATION_PDF)
          .body(outputStream.toByteArray());
  }
  ```

#### 구현 계획

**1단계: 기본 구조 및 JSON 출력 (1-2시간)**
```
domain/todo/
├── controller/
│   └── TodoExportController.java  (신규 생성)
└── service/
    └── TodoExportService.java     (신규 생성)

domain/project/
├── controller/
│   └── ProjectExportController.java  (신규 생성)
└── service/
    └── ProjectExportService.java     (신규 생성)
```

**구현 내용:**
- [ ] TodoExportController 생성
- [ ] TodoExportService 생성
- [ ] JSON 내보내기 엔드포인트 구현
  - `GET /api/todos/{todoId}/export/json` - 단일 TODO
  - `GET /api/todos/export/json` - 필터링된 목록
  - `POST /api/todos/export/json` - 선택된 TODO 목록
- [ ] ProjectExportController 생성
- [ ] 프로젝트 JSON 내보내기 엔드포인트
- [ ] Swagger/OpenAPI 문서화

**2단계: Excel 출력 (3-4시간)**
- [ ] Apache POI 라이브러리 추가 (build.gradle)
- [ ] ExcelGeneratorService 유틸리티 클래스 생성
- [ ] TODO 목록 Excel 생성 메소드 구현
- [ ] Excel 스타일링 (헤더, 색상 코딩)
- [ ] Excel 내보내기 엔드포인트 구현
  - `GET /api/todos/export/excel`
  - `GET /api/projects/{projectId}/export/excel`
  - `POST /api/todos/export/excel`
- [ ] 열 너비 자동 조정 및 최적화
- [ ] Swagger 문서화

**3단계: PDF 출력 (4-5시간)**
- [ ] iText7 라이브러리 추가 (build.gradle)
- [ ] PdfGeneratorService 유틸리티 클래스 생성
- [ ] PDF 템플릿 디자인
  - 헤더, 본문, 메타 정보 레이아웃
  - 프로젝트 색상 반영
- [ ] PDF 생성 메소드 구현
- [ ] PDF 내보내기 엔드포인트 구현
  - `GET /api/todos/{todoId}/export/pdf`
  - `GET /api/projects/{projectId}/export/pdf`
- [ ] Swagger 문서화

#### 추가 고려사항

**1. 보안**
```java
// TodoExportService.java

public TodoResponse exportTodo(Long userId, Long todoId) {
    Todo todo = todoRepository.findById(todoId)
        .orElseThrow(() -> new IllegalArgumentException("TODO를 찾을 수 없습니다"));
    
    // 권한 검증 - 다른 사용자의 데이터 접근 방지
    if (!todo.getUser().getId().equals(userId)) {
        throw new AccessDeniedException("권한이 없습니다.");
    }
    
    return TodoResponse.from(todo);
}
```

**2. 파일명 커스터마이징**
```java
// 의미 있는 파일명 생성
String filename = String.format("todos_%s_%s_%s.xlsx", 
    user.getUsername(), 
    LocalDate.now().format(DateTimeFormatter.ISO_DATE),
    searchRequest.getStatus() != null ? searchRequest.getStatus() : "전체"
);
// 예: todos_홍길동_2025-12-07_진행중.xlsx
```

**3. 비동기 처리 (대용량 데이터)**
```java
@Service
public class TodoExportService {
    
    @Async
    public CompletableFuture<String> exportLargeTodoListAsync(
            Long userId, 
            TodoSearchRequest searchRequest
    ) {
        // 1. 대용량 데이터 조회 및 Excel 생성
        // 2. S3 또는 파일 시스템에 저장
        // 3. 다운로드 링크 생성
        // 4. 이메일 전송 (선택)
        
        return CompletableFuture.completedFuture(downloadUrl);
    }
}
```

**4. Rate Limiting**
```java
// 과도한 내보내기 요청 제한
@RateLimiter(name = "exportApi", fallbackMethod = "exportFallback")
@GetMapping("/export/excel")
public ResponseEntity<byte[]> exportTodosAsExcel(...) {
    // 구현
}
```

**5. 캐싱 전략**
```java
// 동일한 조건의 반복 요청 시 캐시 활용
@Cacheable(
    value = "todoExports", 
    key = "#userId + '_' + #searchRequest.hashCode()"
)
public byte[] generateExcel(Long userId, TodoSearchRequest searchRequest) {
    // Excel 생성 로직
}
```

#### 프론트엔드 연동 방식

**버튼 및 모달 구조:**
- 각 페이지에 "내보내기" 버튼 하나만 배치
- 버튼 클릭 시 `ExportModal.vue` 팝업 표시
- 모달에서 파일 형식 선택 (JSON / Excel / PDF)
- 선택한 형식에 맞는 API 엔드포인트 호출

```typescript
// 프론트엔드 호출 예시
const handleExport = async (format: 'json' | 'excel' | 'pdf') => {
  const response = await fetch(`/api/todos/${todoId}/export/${format}`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  const blob = await response.blob();
  downloadFile(blob, `todo_${todoId}.${format === 'excel' ? 'xlsx' : format}`);
}
```

#### 예상 전체 개발 기간

- **JSON 출력**: 1-2시간
- **Excel 출력**: 3-4시간
- **PDF 출력**: 4-5시간
- **테스트 및 문서화**: 2-3시간
- **총 예상 시간**: 10-14시간

#### 참고 문서

- [Apache POI 공식 문서](https://poi.apache.org/)
- [iText7 공식 문서](https://itextpdf.com/en/products/itext-7)
- [Spring Boot File Download 가이드](https://spring.io/guides/gs/uploading-files/)
- [Spring @Async 문서](https://spring.io/guides/gs/async-method/)

## 🔧 설정

### application.properties

주요 설정 항목:

```properties
# 애플리케이션 이름
spring.application.name=backend

# 데이터베이스 설정
spring.datasource.url=jdbc:mariadb://localhost:3306/todoapp
spring.datasource.username=root
spring.datasource.password=1234

# JPA 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT 설정
jwt.secret=your-secret-key
jwt.expiration=86400000

# CORS 설정
spring.web.cors.allowed-origins=http://localhost:5173
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,PATCH
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true

# OpenAPI/Swagger 설정
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### 환경별 설정

프로덕션 환경에서는 다음을 권장합니다:
- `spring.jpa.hibernate.ddl-auto=validate` 또는 `none`
- `spring.jpa.show-sql=false`
- 강력한 JWT Secret Key 사용
- 데이터베이스 비밀번호 환경 변수로 관리

## 📦 주요 의존성

### 런타임 의존성
- `spring-boot-starter-web` - Spring MVC
- `spring-boot-starter-security` - Spring Security
- `spring-boot-starter-data-jpa` - JPA/Hibernate
- `spring-boot-starter-validation` - Bean Validation
- `spring-boot-starter-jooq` - JOOQ
- `mariadb-java-client` - MariaDB 드라이버
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` - JWT 라이브러리
- `springdoc-openapi-starter-webmvc-ui` - Swagger UI

### 개발 의존성
- `lombok` - 보일러플레이트 코드 제거
- `spring-boot-devtools` - 개발 도구
- `spring-boot-starter-test` - 테스트

## 📚 API 문서

### Swagger UI

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

Swagger UI에서:
- 모든 API 엔드포인트 확인
- 요청/응답 스키마 확인
- 직접 API 테스트 가능
- JWT 토큰 인증 테스트 가능

### OpenAPI 스펙

프론트엔드에서 OpenAPI 스펙을 사용하여 클라이언트 코드를 자동 생성할 수 있습니다:
- OpenAPI JSON: http://localhost:8080/api-docs

## 🧪 테스트

### 테스트 구조

프로젝트는 다음 테스트 구조를 가지고 있습니다:

```
src/test/java/com/TodoApp/backend/
├── BackendApplicationTests.java      # 애플리케이션 컨텍스트 테스트
├── domain/
│   ├── auth/service/
│   │   └── AuthServiceTest.java      # 인증 서비스 단위 테스트 ✅
│   ├── project/service/
│   │   └── ProjectServiceTest.java   # 프로젝트 서비스 단위 테스트 ✅
│   └── todo/service/
│       └── TodoServiceTest.java      # TODO 서비스 단위 테스트 ✅
└── fixture/                          # 테스트 데이터 Fixture
    ├── UserFixture.java
    ├── TodoFixture.java
    ├── ProjectFixture.java
    └── core/                          # Fixture 핵심 유틸리티
```

### 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests BackendApplicationTests
./gradlew test --tests TodoServiceTest

# 테스트 리포트 확인
# build/reports/tests/test/index.html
```

### 테스트 커버리지

현재 구현된 테스트:
- ✅ **AuthServiceTest**: 인증 서비스 로직 테스트
- ✅ **TodoServiceTest**: TODO CRUD 및 검색 로직 테스트
- ✅ **ProjectServiceTest**: 프로젝트 CRUD 로직 테스트

**테스트 전략:**
- 단위 테스트: Service 계층 핵심 로직
- Mock 기반 테스트: Repository 의존성 모킹
- Fixture 패턴: 테스트 데이터 생성 자동화

**향후 계획:**
- 통합 테스트 (REST API 테스트)
- E2E 테스트
- 테스트 커버리지 목표: 70% 이상

## 🐛 문제 해결

### 데이터베이스 연결 실패
```bash
# MariaDB가 실행 중인지 확인
# 데이터베이스가 생성되었는지 확인
# application.properties의 연결 정보 확인
```

### 포트 충돌
```properties
# application.properties에 추가
server.port=8081
```

### JWT 토큰 검증 실패
- JWT Secret Key가 일치하는지 확인
- 토큰이 만료되지 않았는지 확인
- Authorization 헤더 형식 확인: `Bearer {token}`

### CORS 오류
```properties
# application.properties에서 프론트엔드 URL 확인
spring.web.cors.allowed-origins=http://localhost:5173
```

### 쿼리 파라미터 파싱 오류
Spring의 `@ModelAttribute`는 평면 쿼리 파라미터를 기대합니다. 프론트엔드에서 중첩 객체 형식(`searchRequest[page]=0`)이 아닌 평면 형식(`page=0`)으로 전달해야 합니다.

## 🛡️ Null Safety 및 데이터 검증

### Null 처리 전략

이 프로젝트는 타입 안전성과 데이터 무결성을 보장하기 위해 체계적인 Null 처리 전략을 구현했습니다.

#### 1. DTO에서의 Null 처리

**TodoRequest.java**
```java
public class TodoRequest {
    @NotBlank(message = "제목은 필수입니다")
    private String title;  // 필수 필드
    
    @Nullable
    @Schema(nullable = true, types = {"string", "null"})
    private String description;  // 선택적 필드
    
    @Nullable
    @Schema(nullable = true, types = {"string", "null"})
    private Todo.TodoStatus status;  // 선택적 필드 (기본값: TODO)
    
    @Nullable
    @Schema(nullable = true, types = {"string", "null"})
    private Timestamp dueDate;  // 선택적 필드
}
```

**TodoResponse.java**
```java
public class TodoResponse {
    @Nullable
    @Schema(nullable = true, types = {"integer", "null"})
    private Long id;  // 생성 시에는 null
    
    private String title;  // 항상 존재
    
    @Nullable
    @Schema(nullable = true, types = {"string", "null"})
    private String description;  // null 가능
    
    @Nullable
    @Schema(nullable = true, types = {"string", "null"})
    private Timestamp completedAt;  // 완료되지 않은 경우 null
}
```

#### 2. JPA 엔티티에서의 Null 제약

**Todo.java**
```java
@Entity
public class Todo {
    @Column(nullable = false, length = 255)
    private String title;  // NOT NULL 제약
    
    @Column(columnDefinition = "TEXT")
    private String description;  // NULL 허용
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TodoStatus status;  // NOT NULL 제약
    
    @Column(name = "due_date")
    private Timestamp dueDate;  // NULL 허용
    
    @Column(name = "completed_at")
    private Timestamp completedAt;  // NULL 허용
}
```

#### 3. 엔티티 생명주기에서의 Null 처리

**@PrePersist와 @PreUpdate**
```java
@PrePersist
protected void onCreate() {
    createdAt = Timestamp.valueOf(LocalDateTime.now());
    updatedAt = Timestamp.valueOf(LocalDateTime.now());
    
    // null인 경우 기본값 설정
    if (status == null) {
        status = TodoStatus.TODO;
    }
    if (priority == null) {
        priority = Priority.MEDIUM;
    }
}

@PreUpdate
protected void onUpdate() {
    updatedAt = Timestamp.valueOf(LocalDateTime.now());
    
    // 상태 변경에 따른 완료일 자동 관리
    if (status == TodoStatus.DONE && completedAt == null) {
        completedAt = Timestamp.valueOf(LocalDateTime.now());
    }
    if (status != TodoStatus.DONE && completedAt != null) {
        completedAt = null;
    }
}
```

#### 4. OpenAPI 스키마 명세

OpenAPI 스펙에서 nullable 필드를 명확히 정의:

```java
@Schema(
    description = "TODO 설명", 
    example = "JPA와 Security 챕터 복습", 
    nullable = true,  // nullable 명시
    types = {"string", "null"}  // 허용되는 타입 명시
)
@Nullable
private String description;
```

#### 5. DTO 변환에서의 안전한 Null 처리

**TodoResponse.from() 메서드**
```java
public static TodoResponse from(Todo todo) {
    return TodoResponse.builder()
            .id(todo.getId())
            .title(todo.getTitle())
            .description(todo.getDescription())  // null 가능
            .status(todo.getStatus() != null ? todo.getStatus().name() : null)
            .priority(todo.getPriority() != null ? todo.getPriority().name() : null)
            .dueDate(todo.getDueDate())  // null 가능
            .completedAt(todo.getCompletedAt())  // null 가능
            .build();
}
```

### Bean Validation 활용

```java
public class TodoRequest {
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    private String title;  // 필수 + 길이 제한
    
    @Nullable  // 명시적 null 허용
    private String description;  // 선택적 필드
}
```

### 장점

1. **타입 안전성**: `@Nullable` 어노테이션으로 null 가능성 명시
2. **API 문서화**: OpenAPI 스펙에서 nullable 필드 자동 문서화
3. **데이터 무결성**: JPA 제약 조건으로 데이터베이스 레벨 보장
4. **자동 처리**: 엔티티 생명주기에서 null 값 자동 관리
5. **검증**: Bean Validation으로 요청 데이터 검증

## 🚨 예외 처리 시스템

### 커스텀 예외 처리 체계 ✅

프로젝트는 체계적인 예외 처리 시스템을 구축하여 일관된 에러 응답과 명확한 에러 코드를 제공합니다.

#### 1. ErrorCode Enum

모든 도메인별 에러 코드를 중앙에서 관리합니다.

```java
// global/exception/ErrorCode.java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common (공통)
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(400, "잘못된 입력값입니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "권한이 없습니다."),
    
    // User (사용자)
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    DUPLICATE_USERNAME(409, "이미 존재하는 사용자명입니다."),
    
    // Todo (할 일)
    TODO_NOT_FOUND(404, "TODO를 찾을 수 없습니다."),
    TODO_ACCESS_DENIED(403, "TODO에 접근할 권한이 없습니다."),
    
    // Project (프로젝트)
    PROJECT_NOT_FOUND(404, "프로젝트를 찾을 수 없습니다."),
    PROJECT_NAME_DUPLICATE(409, "이미 존재하는 프로젝트명입니다."),
    DEFAULT_PROJECT_DELETE_NOT_ALLOWED(400, "기본 프로젝트는 삭제할 수 없습니다.");
    
    private final int status;
    private final String message;
}
```

#### 2. BusinessException

비즈니스 로직 예외를 위한 커스텀 예외 클래스입니다.

```java
// global/exception/BusinessException.java
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
```

#### 3. ErrorResponse DTO

일관된 에러 응답 형식을 제공합니다.

```java
// global/common/dto/ErrorResponse.java
@Getter
@Builder
public class ErrorResponse {
    private boolean success;      // 항상 false
    private int status;           // HTTP 상태 코드
    private String message;       // 에러 메시지
    private String code;          // 에러 코드 (enum 이름)
    private LocalDateTime timestamp;  // 발생 시각
}
```

**에러 응답 예시:**
```json
{
  "success": false,
  "status": 404,
  "message": "TODO를 찾을 수 없습니다.",
  "code": "TODO_NOT_FOUND",
  "timestamp": "2025-12-12T10:30:00"
}
```

#### 4. GlobalExceptionHandler

모든 예외를 전역에서 처리합니다.

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: code={}, message={}", ex.getErrorCode(), ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode());
        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }
    
    // Bean Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(...) {
        // 필드별 검증 에러 처리
    }
    
    // Spring Security 접근 거부 예외
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(...) {
        // 403 Forbidden 처리
    }
    
    // 전역 예외 처리 (최종 방어선)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        return ResponseEntity.status(500).body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
```

#### 5. Service 계층에서의 사용

```java
// TodoService.java
public TodoResponse getTodo(Long userId, Long todoId) {
    Todo todo = todoRepository.findByIdAndUserId(todoId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.TODO_NOT_FOUND));
    return TodoResponse.from(todo);
}

// AuthService.java
public AuthResponse signup(SignupRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
        throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
    }
    // 회원가입 로직...
}

// ProjectService.java
public void deleteProject(Long projectId, User user) {
    Project project = projectRepository.findByIdAndUser(projectId, user)
            .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    
    if (Boolean.TRUE.equals(project.getIsDefault())) {
        throw new BusinessException(ErrorCode.DEFAULT_PROJECT_DELETE_NOT_ALLOWED);
    }
    // 삭제 로직...
}
```

#### 6. 새로운 예외 추가 방법

**Step 1: ErrorCode에 추가**
```java
// ErrorCode.java
FILE_UPLOAD_FAILED(400, "파일 업로드에 실패했습니다."),
FILE_SIZE_EXCEEDED(413, "파일 크기가 제한을 초과했습니다."),
```

**Step 2: Service에서 사용**
```java
if (file.getSize() > MAX_FILE_SIZE) {
    throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
}
```

**Step 3: (선택) 특정 예외 핸들러 추가**
```java
// GlobalExceptionHandler.java
@ExceptionHandler(MaxUploadSizeExceededException.class)
public ResponseEntity<ErrorResponse> handleFileSizeException(Exception ex) {
    ErrorResponse errorResponse = ErrorResponse.of(ErrorCode.FILE_SIZE_EXCEEDED);
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
}
```

### 예외 처리 시스템의 장점

✅ **타입 안전성**: ErrorCode enum으로 컴파일 타임 검증  
✅ **일관성**: 모든 API가 동일한 에러 형식 반환  
✅ **명확성**: 에러 코드로 클라이언트가 에러를 쉽게 구분  
✅ **유지보수성**: 에러 메시지 중앙 관리  
✅ **로깅**: 예외 발생 시 자동 로깅  
✅ **확장성**: 새로운 에러 코드 추가가 간단함  
✅ **국제화 지원**: 에러 코드 기반으로 다국어 메시지 제공 가능

## 🔄 Git 워크플로우

이 프로젝트는 독립적인 Git 레포지토리입니다.

```bash
# 초기 커밋 (이미 완료된 경우 생략)
git add .
git commit -m "Initial commit: Backend setup"

# 원격 저장소 연결 (선택사항)
git remote add origin <백엔드-저장소-URL>
git branch -M main
git push -u origin main
```

## 📝 개발 가이드

### 새로운 API 추가하기

1. **Controller 생성**
```java
@RestController
@RequestMapping("/api/example")
@RequiredArgsConstructor
@Tag(name = "Example", description = "예제 API")
public class ExampleController {
    
    @GetMapping
    @Operation(summary = "예제 조회")
    public ResponseEntity<ApiResponse<ExampleResponse>> getExample() {
        // 구현
    }
}
```

2. **Service 생성**
```java
@Service
@RequiredArgsConstructor
public class ExampleService {
    // 비즈니스 로직 구현
}
```

3. **DTO 생성**
```java
@Getter
@Setter
public class ExampleRequest {
    @NotBlank
    private String name;
}
```

### 예외 처리

전역 예외 핸들러(`GlobalExceptionHandler`)에서 예외를 처리합니다:
- `@ExceptionHandler`로 특정 예외 처리
- 공통 응답 형식으로 변환

## 🚀 배포

### JAR 파일 빌드
```bash
./gradlew bootJar
```

생성된 JAR 파일: `build/libs/backend-0.0.1-SNAPSHOT.jar`

### 실행
```bash
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

### 프로덕션 설정
- 환경 변수로 민감 정보 관리
- 로깅 설정 구성
- 데이터베이스 연결 풀 최적화
- HTTPS 설정

## 📚 참고 문서

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [Spring Security 공식 문서](https://spring.io/projects/spring-security)
- [Spring Data JPA 공식 문서](https://spring.io/projects/spring-data-jpa)
- [SpringDoc OpenAPI 공식 문서](https://springdoc.org/)
- [JWT.io](https://jwt.io/) - JWT 디버깅 및 정보
- [MariaDB 공식 문서](https://mariadb.com/kb/en/documentation/)

## 📝 라이선스

이 프로젝트는 독립적으로 관리되며, 프론트엔드와 별도의 라이선스를 가질 수 있습니다.
