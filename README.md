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

- [x] **예외 처리**
  - 전역 예외 핸들러 (`GlobalExceptionHandler`)
  - 공통 에러 응답 형식 (`ApiResponse`)
  - Bean Validation 유효성 검사
  - 사용자 친화적 에러 메시지

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

### 📤 Phase 4 예정 - 파일 출력(Export) 기능

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
