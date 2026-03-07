FROM amazoncorretto:21 AS builder

RUN yum install -y findutils

WORKDIR /workspace

COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle.kts .
COPY build.gradle.kts .

COPY domain/model/build.gradle.kts domain/model/
COPY domain/usecase/build.gradle.kts domain/usecase/
COPY infrastructure/entrypoint-rest/build.gradle.kts infrastructure/entrypoint-rest/
COPY infrastructure/postgres-db/build.gradle.kts infrastructure/postgres-db/
COPY infrastructure/client-rest/build.gradle.kts infrastructure/client-rest/
COPY infrastructure/s3-storage/build.gradle.kts infrastructure/s3-storage/
COPY applications/pet/build.gradle.kts applications/pet/

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q || true

COPY . .

RUN ./gradlew :pet:bootJar -x test --no-daemon

FROM amazoncorretto:21-alpine3.22

WORKDIR /app

RUN apk add --no-cache curl && \
    addgroup -g 1001 appgroup && \
    adduser -D -s /bin/sh -u 1001 -G appgroup appuser

COPY --from=builder /workspace/applications/pet/build/libs/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
