# [Phase 4] JOOQ 타입 안전성 개선

## 개요

JOOQ 코드 생성을 통해 문자열 기반 필드명을 타입 안전한 코드로 변경합니다.

## 우선순위
추가 개선사항

## 예상 소요 시간
4-5시간

## 현재 문제
- TodoRepositoryImpl에서 문자열 기반 필드명 사용
- 리팩토링 시 런타임 오류 위험

## 구현 내용

### 1. JOOQ Gradle 플러그인 설정
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

### 2. TodoRepositoryImpl 리팩토링
- 문자열 필드명을 타입 안전 코드로 변경
- 컴파일 타임 검증

## 체크리스트
- [ ] JOOQ Gradle 플러그인 설정
- [ ] 코드 생성 실행
- [ ] TodoRepositoryImpl 리팩토링
- [ ] 문자열 필드명을 타입 안전 코드로 변경
- [ ] 빌드 스크립트 업데이트
- [ ] 테스트 코드 검증

## 장점
- 컴파일 타임 타입 안전성
- IDE 자동완성 지원
- 리팩토링 안정성

