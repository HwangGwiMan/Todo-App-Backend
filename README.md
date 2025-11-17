# TodoApp Backend

Spring Boot 3.5.7 + Java 17로 구축된 TodoApp 백엔드 API 서버입니다.

## 📋 프로젝트 정보

이 프로젝트는 독립적인 Git 레포지토리로 관리됩니다. 프론트엔드와 별도로 버전 관리됩니다.

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
│   ├── project/                     # 프로젝트 도메인 (Phase 2)
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
    │       └── MessageResponse.java
    ├── config/
    │   ├── OpenApiConfig.java       # Swagger/OpenAPI 설정
    │   └── SecurityConfig.java     # Spring Security 설정
    ├── exception/
    │   └── GlobalExceptionHandler.java  # 전역 예외 처리
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

### 프로젝트 API (Phase 2)

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
- `projectId`: 프로젝트 ID 필터 (Phase 2)
- `status`: 상태 필터 (TODO, IN_PROGRESS, DONE)
- `priority`: 우선순위 필터 (HIGH, MEDIUM, LOW)
- `sortBy`: 정렬 필드 (createdAt, dueDate, priority, position, title)
- `sortDirection`: 정렬 방향 (ASC, DESC)
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기

예시:
```
GET /api/todos?projectId=1&status=TODO&priority=HIGH&sortBy=createdAt&sortDirection=DESC&page=0&size=20
```

**참고**: Spring의 `@ModelAttribute`는 평면 쿼리 파라미터를 기대합니다. 프론트엔드에서 중첩 객체(`searchRequest[page]=0`) 형식이 아닌 평면 형식(`page=0`)으로 전달해야 합니다.

## 🎯 개발 진행 상황

### ✅ Phase 1 완료 (2025년 11월)

**구현 완료된 기능:**

- [x] **인증 시스템**
  - JWT 기반 인증
  - 회원가입/로그인 API
  - 인증 필터 및 보안 설정
  - 사용자 정보 관리

- [x] **TODO CRUD API**
  - TODO 생성 (`POST /api/todos`)
  - TODO 조회 (`GET /api/todos`, `GET /api/todos/{id}`)
  - TODO 수정 (`PUT /api/todos/{id}`)
  - TODO 상태 변경 (`PATCH /api/todos/{id}/status`)
  - TODO 삭제 (`DELETE /api/todos/{id}`)

- [x] **검색 및 필터링**
  - 키워드 검색 (제목, 설명)
  - 상태 필터링 (TODO, IN_PROGRESS, DONE)
  - 우선순위 필터링 (HIGH, MEDIUM, LOW)
  - 정렬 기능 (생성일, 마감일, 우선순위, 제목)
  - 페이징 지원

- [x] **통계 API**
  - 사용자별 TODO 통계 (`GET /api/todos/stats`)
  - 전체, 할 일, 진행중, 완료 개수
  - 완료율 계산

- [x] **API 문서화**
  - OpenAPI/Swagger 통합
  - Swagger UI 제공
  - API 스펙 자동 생성

- [x] **예외 처리**
  - 전역 예외 핸들러
  - 공통 에러 응답 형식
  - 유효성 검사

- [x] **데이터베이스**
  - MariaDB 연동
  - JPA/Hibernate 사용
  - 엔티티 관계 설정

### ✅ Phase 2 완료 (2025년 11월)

**구현 완료된 기능:**

- [x] **프로젝트 기능**
  - 프로젝트 엔티티 및 CRUD API
  - 프로젝트별 TODO 그룹화 (`projectId` 필터)
  - 기본 프로젝트 관리 (색상, 순서 등)
  - 프로젝트 삭제 시 관련 TODO 처리

- [x] **확장된 검색 및 필터링**
  - 프로젝트 ID 필터링 지원
  - TODO-프로젝트 연관 관계 구현

- [x] **데이터 무결성**
  - 프로젝트-TODO 관계 설정
  - 기본 프로젝트 관리 로직
  - CASCADE 처리 및 NULL 안전성

### 🚧 Phase 3 예정

**다음 단계 구현 예정:**

- [ ] **고급 검색 기능**
  - 날짜 범위 검색 (마감일)
  - 복합 필터 조합
  - 저장된 검색 조건

- [ ] **TODO 고급 기능**
  - TODO 순서 변경 (position)
  - TODO 복제
  - TODO 템플릿
  - TODO 태그 기능

- [ ] **성능 최적화**
  - 쿼리 최적화
  - 캐싱 전략
  - 인덱스 최적화

- [ ] **보안 강화**
  - 비밀번호 정책 강화
  - Rate Limiting
  - CSRF 보호

- [ ] **테스트**
  - 단위 테스트
  - 통합 테스트
  - API 테스트

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

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests BackendApplicationTests
```

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
