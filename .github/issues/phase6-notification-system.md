# [Phase 6] TODO 일정 관리 및 알림 기능

## 개요

TODO에 상세한 일정 관리 필드를 추가하고, 카카오톡/SMS/이메일을 통한 알림 기능을 구현합니다.

## 예상 소요 시간
27-35시간

## 주요 기능

### 1. 일정 관리 필드 확장 (4-5시간)
- Todo 엔티티에 일정 관련 필드 추가
  - startDate: 일정 시작 일시
  - endDate: 일정 종료 일시
  - isAllDay: 종일 일정 여부
  - recurrenceRule: 반복 일정 설정 (JSON)
  - location: 일정 위치 정보
  - estimatedDuration: 예상 소요 시간

### 2. 알림 시스템 구조
새로운 도메인 패키지: `domain/notification/`
- NotificationController
- NotificationService
- EmailNotificationService
- SmsNotificationService
- KakaoNotificationService
- NotificationScheduler

### 3. 알림 서비스 구현

#### 3-1. 이메일 알림 (3-4시간)
- JavaMailSender 설정
- EmailNotificationService 구현
- HTML 이메일 템플릿 작성
- 테스트 API 구현

#### 3-2. 알림 스케줄러 (4-5시간)
- NotificationLog 엔티티 및 Repository
- Spring Scheduler 구현
- 알림 설정 파싱 로직
- 중복 발송 방지 로직

#### 3-3. SMS 알림 (3-4시간)
- SMS API 업체 선택 (NHN Cloud / Twilio / 알리고 / 솔라피)
- SmsNotificationService 구현
- 테스트 및 에러 처리

#### 3-4. 카카오톡 알림톡 (5-6시간)
- 카카오 비즈니스 계정 등록
- 알림톡 템플릿 작성 및 승인 요청
- KakaoNotificationService 구현
- WebClient를 사용한 API 연동

#### 3-5. 반복 일정 (6-8시간)
- RecurrenceService 구현
- 반복 규칙 파싱 및 검증
- 다음 발생 날짜 계산 로직
- 반복 일정 미리보기 API

#### 3-6. 사용자 알림 설정 (2-3시간)
- NotificationSetting 엔티티
- 사용자별 알림 설정 CRUD API
- 전역 알림 on/off 기능

## API 엔드포인트 추가

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/notifications/settings` | 사용자 알림 설정 조회 |
| PUT | `/api/notifications/settings` | 사용자 알림 설정 수정 |
| POST | `/api/notifications/test` | 테스트 알림 발송 |
| GET | `/api/notifications/logs` | 알림 발송 이력 조회 |
| GET | `/api/todos/{id}/recurrence` | 반복 일정 미리보기 |

## 환경 설정

### application.yml
```yaml
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
```

## 체크리스트

### Phase 6-1: 일정 필드 확장
- [ ] Todo 엔티티 확장
- [ ] DTO 및 매핑 로직 수정
- [ ] 데이터베이스 마이그레이션
- [ ] API 문서 업데이트

### Phase 6-2: 이메일 알림
- [ ] JavaMailSender 설정
- [ ] EmailNotificationService 구현
- [ ] HTML 템플릿 작성
- [ ] 테스트 API

### Phase 6-3: 알림 스케줄러
- [ ] NotificationLog 엔티티
- [ ] NotificationScheduler 구현
- [ ] 알림 설정 파싱
- [ ] 중복 발송 방지

### Phase 6-4: SMS 알림
- [ ] SMS API 업체 선택
- [ ] SmsNotificationService 구현
- [ ] 테스트 및 에러 처리

### Phase 6-5: 카카오톡 알림톡
- [ ] 카카오 비즈니스 계정
- [ ] 템플릿 승인
- [ ] KakaoNotificationService 구현
- [ ] API 연동 테스트

### Phase 6-6: 반복 일정
- [ ] RecurrenceService 구현
- [ ] 반복 규칙 파싱
- [ ] 날짜 계산 로직
- [ ] 미리보기 API

### Phase 6-7: 사용자 설정
- [ ] NotificationSetting 엔티티
- [ ] 설정 CRUD API
- [ ] 전역 on/off 기능

## 참고 문서
- [Kakao Notification API](https://developers.kakao.com/docs/latest/ko/message/rest-api)
- [NHN Cloud SMS API](https://docs.nhncloud.com/ko/Notification/SMS/ko/api-guide/)
- [Spring Scheduler](https://spring.io/guides/gs/scheduling-tasks/)
- [Spring Mail](https://docs.spring.io/spring-framework/reference/integration/email.html)

