# =========================
# 빌드용 이미지
# =========================
# 프로젝트 내장 Gradle Wrapper를 사용하기 위해 표준 JDK 이미지 사용
FROM eclipse-temurin:25-jdk AS builder

# 작업 디렉토리 설정
WORKDIR /build

# 1. Gradle Wrapper 및 설정 파일만 먼저 복사 (의존성 캐싱 목적)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# 2. 소스 코드 복사
COPY src src

# 3. 내장 Gradle Wrapper로 jar 빌드 (테스트 제외)
RUN ./gradlew clean bootJar -x test

# =========================
# 실행용 이미지
# =========================
# 실제 실행은 가벼운 JRE 이미지 사용
# 경량 모델 사용 X
FROM eclipse-temurin:25-jre

# 작업 디렉토리 설정
WORKDIR /app

# 한국 시간대 설정
ENV TZ=Asia/Seoul

# 빌드 단계에서 만들어진 jar 파일만 복사
COPY --from=builder /build/build/libs/*.jar app.jar

# 애플리케이션 포트
EXPOSE 8080

# 컨테이너 시작 명령
ENTRYPOINT ["java", "-jar", "app.jar"]