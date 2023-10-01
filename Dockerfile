#BUILDING STAGE
FROM maven:3.8.4-openjdk-17-slim AS build
#ADD repository.tar.gz /usr/share/maven/ref/
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean scala:compile package spring-boot:repackage

# USING PREVIOUS STAGE
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
EXPOSE 8080 80
COPY --from=BUILD /build/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/app/app.jar"]