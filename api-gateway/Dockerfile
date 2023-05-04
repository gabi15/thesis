FROM --platform=linux/amd64 amazoncorretto:17-alpine-jdk
ARG JAR_FILE=/build/libs/*.jar
COPY ${JAR_FILE} gateway.jar
ENTRYPOINT ["java", "-jar","/gateway.jar"]