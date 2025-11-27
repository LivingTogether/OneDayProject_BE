# 1. Base Image: Eclipse Temurin (Java 21 표준)
FROM eclipse-temurin:21-jdk

# 2. JAR 파일 위치 변수 설정
ARG JAR_FILE=build/libs/*.jar

# 3. JAR 파일을 컨테이너 안으로 복사
COPY ${JAR_FILE} app.jar

# 4. 실행 명령어
ENTRYPOINT ["java", "-jar", "/app.jar"]