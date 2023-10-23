#BUILDING STAGE
#this jdk allowing not specify sturtup command in Azure App Service
FROM maven:3.8.4-openjdk-17-slim AS build
#ADD repository.tar.gz /usr/share/maven/ref/
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean scala:compile package spring-boot:repackage

# USING PREVIOUS STAGE
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
COPY --from=BUILD /build/target/*.jar /app/app.jar
EXPOSE 443:443
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/app/app.jar"]