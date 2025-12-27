# [Phase 5] 파일 출력(Export) 기능

## 개요

TODO 및 프로젝트 데이터를 다양한 파일 형식(JSON, Excel, PDF)으로 내보내기할 수 있는 기능을 추가합니다.

## 예상 소요 시간
10-14시간

## 지원 파일 형식

### 1. JSON 출력 (1-2시간) ⭐ 높음
- 구현 난이도: 낮음
- 필요 라이브러리: 없음 (Spring Boot 기본 제공)
- 즉시 사용 가능

### 2. Excel 출력 (3-4시간) ⭐ 높음
- 구현 난이도: 중간
- 필요 라이브러리: Apache POI
- 스타일링 지원

### 3. PDF 출력 (4-5시간) ⭐ 중간
- 구현 난이도: 높음
- 필요 라이브러리: iText7
- 한글 폰트 지원 필요

## 구현 계획

### 1단계: 기본 구조 및 JSON 출력 (1-2시간)

새로운 컨트롤러 및 서비스:
```
domain/todo/
├── controller/
│   └── TodoExportController.java
└── service/
    └── TodoExportService.java

domain/project/
├── controller/
│   └── ProjectExportController.java
└── service/
    └── ProjectExportService.java
```

**API 엔드포인트:**
- `GET /api/todos/{todoId}/export/json` - 단일 TODO
- `GET /api/todos/export/json` - 필터링된 목록
- `POST /api/todos/export/json` - 선택된 TODO 목록

### 2단계: Excel 출력 (3-4시간)

**의존성 추가:**
```gradle
implementation 'org.apache.poi:poi:5.2.5'
implementation 'org.apache.poi:poi-ooxml:5.2.5'
```

**Excel 스타일링:**
- 헤더 행: 굵게, 배경색 (회색)
- 상태별 색상 코딩
  - TODO: 파랑
  - IN_PROGRESS: 주황
  - DONE: 초록
- 우선순위별 색상
  - HIGH: 빨강
  - MEDIUM: 노랑
  - LOW: 회색

**API 엔드포인트:**
- `GET /api/todos/export/excel`
- `GET /api/projects/{projectId}/export/excel`
- `POST /api/todos/export/excel`

### 3단계: PDF 출력 (4-5시간)

**의존성 추가:**
```gradle
implementation 'com.itextpdf:itext7-core:7.2.5'
```

**PDF 구성:**
- 제목 및 메타 정보
- 테이블 레이아웃
- 프로젝트 색상 반영
- 한글 폰트 지원

**API 엔드포인트:**
- `GET /api/todos/{todoId}/export/pdf`
- `GET /api/projects/{projectId}/export/pdf`

## 추가 고려사항

### 1. 보안
```java
public TodoResponse exportTodo(Long userId, Long todoId) {
    Todo todo = todoRepository.findById(todoId)
        .orElseThrow(() -> new BusinessException(ErrorCode.TODO_NOT_FOUND));
    
    // 권한 검증
    if (!todo.getUser().getId().equals(userId)) {
        throw new BusinessException(ErrorCode.TODO_ACCESS_DENIED);
    }
    
    return TodoResponse.from(todo);
}
```

### 2. 파일명 커스터마이징
```java
String filename = String.format("todos_%s_%s_%s.xlsx", 
    user.getUsername(), 
    LocalDate.now().format(DateTimeFormatter.ISO_DATE),
    searchRequest.getStatus() != null ? searchRequest.getStatus() : "전체"
);
```

### 3. 비동기 처리 (대용량 데이터)
```java
@Async
public CompletableFuture<String> exportLargeTodoListAsync(
        Long userId, 
        TodoSearchRequest searchRequest
) {
    // S3 또는 파일 시스템에 저장
    // 다운로드 링크 생성
    // 이메일 전송 (선택)
}
```

### 4. Rate Limiting
```java
@RateLimiter(name = "exportApi", fallbackMethod = "exportFallback")
@GetMapping("/export/excel")
public ResponseEntity<byte[]> exportTodosAsExcel(...) {
    // 과도한 요청 제한
}
```

### 5. 캐싱 전략
```java
@Cacheable(
    value = "todoExports", 
    key = "#userId + '_' + #searchRequest.hashCode()"
)
public byte[] generateExcel(Long userId, TodoSearchRequest searchRequest) {
    // 동일 조건 반복 요청 시 캐시 활용
}
```

## 체크리스트

### JSON 출력
- [ ] TodoExportController 생성
- [ ] TodoExportService 생성
- [ ] JSON 내보내기 엔드포인트 구현
- [ ] ProjectExportController 생성
- [ ] Swagger 문서화

### Excel 출력
- [ ] Apache POI 라이브러리 추가
- [ ] ExcelGeneratorService 유틸리티 생성
- [ ] Excel 생성 메소드 구현
- [ ] Excel 스타일링
- [ ] Excel 엔드포인트 구현
- [ ] Swagger 문서화

### PDF 출력
- [ ] iText7 라이브러리 추가
- [ ] PdfGeneratorService 유틸리티 생성
- [ ] PDF 템플릿 디자인
- [ ] PDF 생성 메소드 구현
- [ ] PDF 엔드포인트 구현
- [ ] Swagger 문서화

### 테스트 및 문서화
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] API 문서 업데이트
- [ ] 성능 테스트

## 참고 문서
- [Apache POI](https://poi.apache.org/)
- [iText7](https://itextpdf.com/en/products/itext-7)
- [Spring Boot File Download](https://spring.io/guides/gs/uploading-files/)
- [Spring @Async](https://spring.io/guides/gs/async-method/)

