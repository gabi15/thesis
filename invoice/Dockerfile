FROM --platform=linux/amd64 amazoncorretto:17-alpine-jdk
ARG JAR_FILE=/build/libs/*.jar
COPY ${JAR_FILE} invoice.jar
ENTRYPOINT ["java", "-jar","/invoice.jar"]