#BUILDING STAGE
FROM maven:3.8.6-amazoncorretto-18 as BUILD
#ADD repository.tar.gz /usr/share/maven/ref/
COPY src/ /usr/src/app/src
COPY pom.xml /usr/src/app
WORKDIR /usr/src/app
RUN mvn clean scala:compile package spring-boot:repackage && jar tf target/app-1.jar

# USING PREVIOUS STAGE
FROM amazoncorretto:18
EXPOSE 8080 5005
COPY --from=BUILD /usr/src/app/target /opt/target
WORKDIR /opt/target
#ENV _JAVA_OPTIONS '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005'
CMD ["java", "-jar", "app-1.jar"]