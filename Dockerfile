#BUILDING STAGE
FROM maven:3.8.6-amazoncorretto-18 as BUILD
#ADD repository.tar.gz /usr/share/maven/ref/
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean scala:compile package spring-boot:repackage

# USING PREVIOUS STAGE
FROM amazoncorretto:18
EXPOSE 8080 80
COPY --from=BUILD /build/target/*.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/app/app.jar"]