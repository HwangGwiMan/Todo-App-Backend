# [Phase 4] 입력 검증 강화

## 개요

Bean Validation을 활용하여 모든 Request DTO에 대한 입력 검증을 강화합니다.

## 우선순위
추가 개선사항

## 예상 소요 시간
2-3시간

## 구현 내용

### 1. TodoRequest 개선
```java
public class TodoRequest {
    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 255, message = "제목은 1-255자여야 합니다")
    private String title;
    
    @Size(max = 5000, message = "설명은 5000자 이하여야 합니다")
    private String description;
    
    @Min(value = 0, message = "position은 0 이상이어야 합니다")
    private Integer position;
}
```

### 2. ProjectRequest 개선
```java
public class ProjectRequest {
    @NotBlank
    @Size(min = 1, max = 100)
    private String name;
    
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "유효하지 않은 색상 코드")
    private String color;
}
```

### 3. Custom Validator 작성
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FutureDateValidator.class)
public @interface FutureDate {
    String message() default "마감일은 현재 시간 이후여야 합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

## 체크리스트
- [ ] 모든 Request DTO에 validation 어노테이션 추가
  - [ ] TodoRequest
  - [ ] ProjectRequest
  - [ ] SignupRequest
  - [ ] LoginRequest
- [ ] Custom Validator 작성 (필요 시)
- [ ] 에러 메시지 한글화
- [ ] Validation 실패 테스트 작성
- [ ] GlobalExceptionHandler에서 MethodArgumentNotValidException 처리 개선

## 검증 규칙

### TodoRequest
- title: 필수, 1-255자
- description: 선택, 5000자 이하
- dueDate: 선택, 미래 날짜
- priority: 선택, ENUM 값
- status: 선택, ENUM 값
- position: 선택, 0 이상

### ProjectRequest
- name: 필수, 1-100자, 중복 불가
- color: 선택, 유효한 HEX 색상 코드
- description: 선택, 500자 이하
- position: 선택, 0 이상

