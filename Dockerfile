FROM openjdk:17-jdk-slim

WORKDIR /app

# CI에서 빌드된 JAR 파일을 복사 (경로는 프로젝트 구조에 따라 조정 필요)
COPY build/libs/*.jar app.jar

# 운영 프로파일 적용 및 환경변수 주입을 위한 ENTRYPOINT 설정
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

