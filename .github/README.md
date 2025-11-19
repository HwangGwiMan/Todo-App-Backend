# Backend GitHub Actions 워크플로우

이 디렉토리에는 Backend 저장소의 CI/CD 파이프라인 설정이 포함되어 있습니다.

## 워크플로우 파일 설명

### 1. `ci.yml` - CI 파이프라인
- **트리거**: `main`, `develop` 브랜치에 push 또는 PR 생성 시
- **작업**:
  - MariaDB 서비스 컨테이너 실행
  - Java 17 환경 설정
  - Gradle 테스트 실행
  - 애플리케이션 빌드
  - 테스트 결과 및 빌드 아티팩트 업로드

### 2. `cd.yml` - 배포 파이프라인
- **트리거**: `main` 브랜치에 push 또는 버전 태그(`v*`) 생성 시
- **작업**:
  - 애플리케이션 빌드
  - 릴리스 패키지 생성
  - 아티팩트 업로드
  - (선택적) 서버 배포

## 설정 방법

### GitHub Secrets 설정

배포를 위해 다음 Secrets를 GitHub 저장소에 설정해야 합니다:

1. GitHub 저장소로 이동
2. Settings → Secrets and variables → Actions
3. New repository secret 클릭
4. 다음 Secrets 추가:

```
DEPLOY_HOST          # 배포 서버 호스트 주소
DEPLOY_USER          # 배포 서버 사용자명
DEPLOY_SSH_KEY       # 배포 서버 SSH 개인 키
```

### 배포 스크립트 활성화

`cd.yml` 파일에서 배포 스크립트의 주석을 해제하고 실제 환경에 맞게 수정하세요.

## 워크플로우 실행 확인

1. GitHub 저장소의 **Actions** 탭으로 이동
2. 워크플로우 실행 상태 확인
3. 실패한 작업 클릭하여 로그 확인

