# 기존: FROM openjdk:17-jdk-slim
# 변경: 공식 지원되는 eclipse-temurin 이미지 사용
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# CI에서 빌드된 JAR 파일을 복사
COPY build/libs/*.jar app.jar

# 운영 프로파일 적용
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

