FROM openjdk:17-jdk-slim

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# ENTRYPOINT 수정: 환경변수가 실행 시점에 반영되도록
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=$PROFILES -Dserver.env=$ENV -jar app.jar"]

