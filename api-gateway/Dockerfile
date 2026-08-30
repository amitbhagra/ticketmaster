# ---- Build stage ----
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml

RUN mvn -q -f api-gateway/pom.xml -DskipTests dependency:go-offline

COPY api-gateway/src ./api-gateway/src
RUN mvn -q -f api-gateway/pom.xml -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/api-gateway/target/api-gateway-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]