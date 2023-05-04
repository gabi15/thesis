FROM --platform=linux/amd64 amazoncorretto:17-alpine-jdk
ARG JAR_FILE=/build/libs/*.jar
COPY ${JAR_FILE} users.jar
ENTRYPOINT ["java", "-jar","/users.jar"]