# =========================
# 빌드용 이미지
# =========================
# Gradle과 JDK가 포함된 이미지 사용
FROM gradle:8.14.3-jdk21 AS builder

# 작업 디렉토리 설정
WORKDIR /build

# 프로젝트 파일 전체 복사
COPY . .

# Gradle로 jar 빌드
# test를 같이 돌리려면 build 사용
RUN gradle clean bootJar
# 빌드 속도를 위해 test를 잠시 제외하려면 -x test 사용 가능
# RUN gradle clean bootJar -x test

# =========================
# 실행용 이미지
# =========================
# 실제 실행은 가벼운 JRE 이미지 사용
# 경량 모델 사용 X
FROM eclipse-temurin:21-jre

# 작업 디렉토리 설정
WORKDIR /app

# 한국 시간대 예시
ENV TZ=Asia/Seoul

# 빌드 단계에서 만들어진 jar 파일만 복사
COPY --from=builder /build/build/libs/*.jar app.jar

# 애플리케이션 포트
EXPOSE 8080

# 컨테이너 시작 명령
ENTRYPOINT ["java", "-jar", "app.jar"]