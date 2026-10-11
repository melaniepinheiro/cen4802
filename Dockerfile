# Build and test the application first with: mvn clean package
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the packaged artifact from the existing Maven build.
COPY target/restaurant-task-tracker-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "--add-modules", "jdk.httpserver", "-jar", "app.jar"]
