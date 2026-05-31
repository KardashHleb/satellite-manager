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
RUN sed -i 's/\r$//' gradlew \
    && chmod +x gradlew \
    && ./gradlew :server:bootJar --no-daemon -x test \
    && rm -rf /root/.gradle

# Run stage
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl \
    && rm -rf /var/cache/apk/*
WORKDIR /app
COPY --from=build /workspace/server/build/libs/server.jar app.jar
RUN addgroup -S app && adduser -S app -G app \
    && chown -R app:app /app
USER app
EXPOSE 8082
ENV SERVER_PORT=8082
HEALTHCHECK --interval=10s --timeout=5s --retries=5 --start-period=40s \
    CMD curl -fsS "http://127.0.0.1:${SERVER_PORT}/actuator/health" || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
