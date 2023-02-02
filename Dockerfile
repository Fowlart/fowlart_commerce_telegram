#BUILDING STAGE
FROM maven:3.5-jdk-8 as BUILD
#ADD repository.tar.gz /usr/share/maven/ref/
COPY src /usr/src/app
COPY pom.xml /usr/src/app
WORKDIR /usr/src/app
RUN mvn package -f pom.xml

# USING PREVIOUS STAGE
FROM openjdk:8-jre
EXPOSE 8080 5005
COPY --from=BUILD /usr/src/app/target /opt/target
WORKDIR /opt/target
#ENV _JAVA_OPTIONS '-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005'
CMD ["java", "-jar", "FowlartCommerce-0.0.1-SNAPSHOT.jar"]