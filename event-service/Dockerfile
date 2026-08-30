# ---- Build stage ----
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY event-service/pom.xml ./event-service/pom.xml

RUN mvn -q -f event-service/pom.xml -DskipTests dependency:go-offline

COPY event-service/src ./event-service/src
RUN mvn -q -f event-service/pom.xml -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/event-service/target/event-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]