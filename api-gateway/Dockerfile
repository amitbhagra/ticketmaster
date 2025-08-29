# Use a lightweight JRE 21 image for the runtime stage
FROM eclipse-temurin:21-jre-alpine
# Copy only the built JAR from the build stage
COPY target/api-gateway-*.jar app.jar
# Expose the port the app runs on
EXPOSE 8091
# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]