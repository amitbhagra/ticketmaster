# ---- Build stage ----
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY payment-service/pom.xml ./payment-service/pom.xml

RUN mvn -q -f payment-service/pom.xml -DskipTests dependency:go-offline

COPY payment-service/src ./payment-service/src
RUN mvn -q -f payment-service/pom.xml -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/payment-service/target/payment-service-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]