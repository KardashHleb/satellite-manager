# Build stage
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle build.gradle ./
COPY telemetry-proto ./telemetry-proto
COPY satellite-events ./satellite-events
COPY server ./server
COPY mission-service ./mission-service
COPY telemetry-service ./telemetry-service
RUN chmod +x gradlew \
    && ./gradlew :telemetry-service:bootJar --no-daemon -x test \
    && rm -rf /root/.gradle

# Run stage
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl \
    && rm -rf /var/cache/apk/*
WORKDIR /app
COPY --from=build /workspace/telemetry-service/build/libs/telemetry-service.jar app.jar
RUN addgroup -S app && adduser -S app -G app \
    && chown -R app:app /app
USER app
EXPOSE 8084 9091
ENV SERVER_PORT=8084
HEALTHCHECK --interval=15s --timeout=5s --retries=5 --start-period=30s \
    CMD curl -fsS "http://127.0.0.1:${SERVER_PORT}/actuator/health" || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
