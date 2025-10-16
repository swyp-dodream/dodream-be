# ==============================================
# 1단계: 빌드 스테이지 (Gradle로 JAR 파일 생성)
# ==============================================
FROM gradle:8.10-jdk21-alpine AS build
WORKDIR /app

# Gradle 설정 파일 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드 (테스트 제외)
RUN gradle clean build -x test --no-daemon

# ==============================================
# 2단계: 실행 스테이지 (경량화된 JRE로 실행)
# ==============================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 보안을 위한 non-root 사용자 생성
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 빌드 스테이지에서 생성된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 포트 노출
EXPOSE 8080

# 애플리케이션 실행 (기본 프로파일: prod)
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:prod}", "app.jar"]
