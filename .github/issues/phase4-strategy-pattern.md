# [Phase 4] Strategy 패턴으로 검색 로직 분리

## 개요

Strategy 패턴을 사용하여 TODO 검색 로직을 분리하고 확장 가능한 구조로 개선합니다.

## 우선순위
중간 (선택사항 - Specification 패턴 구현 시 생략 가능)

## 예상 소요 시간
4-5시간

## 구현 내용

### 1. TodoSearchStrategy 인터페이스 정의
- supports(): 지원 여부 확인
- search(): 검색 실행
- priority(): 우선순위 설정

### 2. 각 검색 조건별 Strategy 구현
- KeywordSearchStrategy
- StatusSearchStrategy
- PrioritySearchStrategy
- ProjectSearchStrategy
- DateRangeSearchStrategy

### 3. TodoService 리팩토링
- 복잡한 if-else 로직을 Strategy 조합으로 변경
- 동적으로 적합한 Strategy 선택

## 체크리스트
- [ ] TodoSearchStrategy 인터페이스 생성
- [ ] KeywordSearchStrategy 구현
- [ ] StatusSearchStrategy 구현
- [ ] PrioritySearchStrategy 구현
- [ ] ProjectSearchStrategy 구현
- [ ] DateRangeSearchStrategy 구현
- [ ] TodoService 리팩토링
- [ ] 단위 테스트 작성

## 참고
- Specification 패턴이 이미 구현되어 있다면 이 패턴은 선택사항입니다.
- 더 복잡한 검색 시나리오가 필요한 경우에만 구현을 고려하세요.

# 종결 (Closure)
**2026년 1월 17일**: 현재 코드베이스 분석 결과, TODO 검색 로직에 이미 Spring Data JPA Specification 패턴이 완벽하게 구현 및 적용되어 있음을 확인했습니다. `TodoService`는 `TodoSpecification`을 통해 동적 쿼리를 효율적으로 처리하고 있으며, 이는 본 이슈에서 Strategy 패턴을 통해 개선하고자 했던 목표를 이미 달성하고 있습니다. 따라서 Strategy 패턴 도입은 중복이며 불필요하다고 판단되어 본 이슈를 종결합니다.

