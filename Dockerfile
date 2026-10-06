# Build the executable Spring Boot WAR with Java 21.
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw --batch-mode --no-transfer-progress -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw --batch-mode --no-transfer-progress -DskipTests package

# Run with a minimal Java 21 image. Distroless runs as UID 65532 by default.
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY --from=build --chown=65532:65532 /workspace/target/*.war /app/app.war

USER 65532:65532
EXPOSE 8080

# Respect container memory limits and exit promptly on an unrecoverable OOM.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.war"]
