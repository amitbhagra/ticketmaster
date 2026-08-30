# ---- Build stage ----
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY events-list-service/pom.xml ./events-list-service/pom.xml

RUN mvn -q -f events-list-service/pom.xml -DskipTests dependency:go-offline

COPY events-list-service/src ./events-list-service/src
RUN mvn -q -f events-list-service/pom.xml -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/events-list-service/target/events-list-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]